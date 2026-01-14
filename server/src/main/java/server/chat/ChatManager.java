package server.chat;

import com.google.gson.Gson;
import server.application.adaptors.UserManagementService;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;
import server.infustructre.adaptors.EmployeeRepository;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;
import shareddto.chat.PendingRequestInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton state manager for chat sessions between branches.
 * Does not handle socket events directly - that's done by chat handlers.
 */
public class ChatManager {
    private static ChatManager instance;
    private final Gson gson = new Gson();

    private UserManagementService userManagementService;
    private EmployeeRepository employeeRepository;

    private final Map<String, String> activeChats = new ConcurrentHashMap<>();
    private final Map<String, List<PendingRequest>> pendingRequests = new ConcurrentHashMap<>();

    // Map of requesterEmail -> Set of targetEmails (to track who they are waiting
    // for)
    private final Map<String, Set<String>> activeRequests = new ConcurrentHashMap<>();

    private ChatManager() {
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void setDependencies(UserManagementService userManagementService,
            EmployeeRepository employeeRepository) {
        this.userManagementService = userManagementService;
        this.employeeRepository = employeeRepository;
    }

    public synchronized void requestBranchChat(String requesterEmail, UUID targetBranchId) {
        List<Employee> branchEmployees = employeeRepository.findByBranchId(targetBranchId);

        // Collect all available employees
        List<String> availableEmails = new ArrayList<>();
        for (Employee emp : branchEmployees) {
            String email = emp.getEmail();
            EmployeeRole role = emp.getRole();

            if (role == EmployeeRole.ADMIN || role == EmployeeRole.SHIFT_MANAGER) {
                continue;
            }

            Optional<Socket> socket = userManagementService.getSocketByEmail(email);
            if (socket.isEmpty()) {
                continue;
            }

            if (activeChats.containsKey(email)) {
                continue;
            }

            availableEmails.add(email);
        }

        if (availableEmails.isEmpty()) {
            sendSystemMessage(requesterEmail, "No employees available in that branch. Try again later.");
            sendCloseConfirmation(requesterEmail);
            return;
        }

        // Initialize activeRequests set for this requester
        activeRequests.put(requesterEmail, ConcurrentHashMap.newKeySet());
        activeRequests.get(requesterEmail).addAll(availableEmails);

        // Send request to ALL available employees
        for (String email : availableEmails) {
            PendingRequest request = new PendingRequest(requesterEmail, email, System.currentTimeMillis());
            pendingRequests.computeIfAbsent(email, k -> new ArrayList<>()).add(request);
//            sendChatRequest(email, requesterEmail);
        }

        sendSystemMessage(requesterEmail,
                "Request sent to " + availableEmails.size() + " employee(s). Waiting for someone to accept...");
    }

    public synchronized boolean acceptChat(String accepterEmail, String targetRequesterEmail) {
        List<PendingRequest> queue = pendingRequests.get(accepterEmail);
        if (queue == null || queue.isEmpty()) {
            sendSystemMessage(accepterEmail, "No pending chat requests found.");
            return false;
        }

        // Find the specific request
        PendingRequest request = null;
        for (PendingRequest r : queue) {
            if (r.getRequesterEmail().equals(targetRequesterEmail)) {
                request = r;
                break;
            }
        }

        if (request == null) {
            sendSystemMessage(accepterEmail, "Request from " + targetRequesterEmail + " no longer exists.");
            return false;
        }

        queue.remove(request);
        if (queue.isEmpty()) {
            pendingRequests.remove(accepterEmail);
        }

        String requesterEmail = request.getRequesterEmail();

        activeRequests.remove(requesterEmail);
        pendingRequests.values().forEach(list -> list.removeIf(r -> r.getRequesterEmail().equals(requesterEmail)));
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        activeChats.put(requesterEmail, accepterEmail);
        activeChats.put(accepterEmail, requesterEmail);

        sendSystemMessage(requesterEmail, "Chat accepted! You are now connected to " + accepterEmail);
        sendSystemMessage(accepterEmail, "You are now connected to " + requesterEmail);

        System.out.println("[ChatManager] Chat established: " + requesterEmail + " <-> " + accepterEmail);
        return true;
    }

    public synchronized void declineChat(String declinerEmail, String targetRequesterEmail) {
        List<PendingRequest> queue = pendingRequests.get(declinerEmail);
        if (queue != null && !queue.isEmpty()) {
            // Find and remove the specific request
            PendingRequest request = null;
            for (PendingRequest r : queue) {
                if (r.getRequesterEmail().equals(targetRequesterEmail)) {
                    request = r;
                    break;
                }
            }

            if (request != null) {
                queue.remove(request);
                if (queue.isEmpty()) {
                    pendingRequests.remove(declinerEmail);
                }

                String requesterEmail = request.getRequesterEmail();

                // Remove decliner from active set
                Set<String> targets = activeRequests.get(requesterEmail);
                if (targets != null) {
                    targets.remove(declinerEmail);

                    // If no more targets left, close the requester's chat mode
                    if (targets.isEmpty()) {
                        activeRequests.remove(requesterEmail);
                        sendSystemMessage(requesterEmail, "All available employees declined your request.");
                        sendCloseConfirmation(requesterEmail);
                    }
                }
            }
        }
    }

    public List<PendingRequest> getPendingRequests(String email) {
        return pendingRequests.get(email);
    }

    /**
     * Get pending request info for a user as a formatted string.
     * Returns requester email of the OLDEST request (FIFO), null otherwise.
     */
    public List<PendingRequestInfo> getPendingRequestInfos(String email) {
        List<PendingRequest> queue = pendingRequests.get(email);
        List<PendingRequestInfo> infos = new ArrayList<>();

        if (queue != null) {
            for (PendingRequest r : queue) {
                infos.add(new PendingRequestInfo(r.getRequesterEmail(), r.getTimestamp()));
            }
        }
        return infos;
    }

    public boolean isInChat(String email) {
        return activeChats.containsKey(email);
    }

    public String getChatPartner(String email) {
        return activeChats.get(email);
    }

    public synchronized void handleMessage(String senderEmail, String message) {
        String partnerEmail = activeChats.get(senderEmail);

        if (partnerEmail == null) {
            sendSystemMessage(senderEmail, "You are not in an active chat.");
            return;
        }

        sendChatMessage(partnerEmail, senderEmail, message);
    }

    public synchronized void closeChat(String email) {
        // 1. Handle active chat cleanup
        String partnerEmail = activeChats.remove(email);
        if (partnerEmail != null) {
            activeChats.remove(partnerEmail);
            sendSystemMessage(partnerEmail, "The other user has left the chat.");
            sendCloseConfirmation(partnerEmail); // Ensure partner also exits chat mode
            System.out.println("[ChatManager] Chat closed: " + email + " <-> " + partnerEmail);
        }

        // 2. Handle pending request cleanup (if this user was a target)
        pendingRequests.remove(email);

        // 3. Handle active request cleanup (if this user was a requester waiting)
        Set<String> targets = activeRequests.remove(email);
        if (targets != null) {
            // Remove the pending request from all targets
            for (String targetEmail : targets) {
                List<PendingRequest> queue = pendingRequests.get(targetEmail);
                if (queue != null) {
                    queue.removeIf(r -> r.getRequesterEmail().equals(email));
                    if (queue.isEmpty()) {
                        pendingRequests.remove(targetEmail);
                    }
                }
            }
            System.out.println("[ChatManager] Cancelled request from " + email);
        }

        sendCloseConfirmation(email);
    }

    private void sendSystemMessage(String email, String message) {
        sendChatMessage(email, null, message);
    }

    private void sendChatRequest(String targetEmail, String requesterEmail) {
        Optional<Socket> socketOpt = userManagementService.getSocketByEmail(targetEmail);
        if (socketOpt.isPresent()) {
            try {
                DataOutputStream out = new DataOutputStream(socketOpt.get().getOutputStream());
                ChatPacket packet = new ChatPacket(null, null, "CHAT_REQUEST from " + requesterEmail);
                SocketMessage msg = new SocketMessage(EventType.CHAT_REQUEST, packet);
                synchronized (out) {
                    out.writeUTF(gson.toJson(msg));
                }
            } catch (IOException e) {
                System.err.println("[ChatManager] Failed to send chat request: " + e.getMessage());
            }
        }
    }

    private void sendChatMessage(String targetEmail, String senderEmail, String message) {
        Optional<Socket> socketOpt = userManagementService.getSocketByEmail(targetEmail);
        if (socketOpt.isPresent()) {
            try {
                DataOutputStream out = new DataOutputStream(socketOpt.get().getOutputStream());
                String content = (senderEmail != null ? senderEmail + ": " : "") + message;
                ChatPacket packet = new ChatPacket(null, null, content);
                SocketMessage msg = new SocketMessage(EventType.CHAT_MESSAGE, packet);
                synchronized (out) {
                    out.writeUTF(gson.toJson(msg));
                }
            } catch (IOException e) {
                System.err.println("[ChatManager] Failed to send message: " + e.getMessage());
            }
        }
    }

    private void sendCloseConfirmation(String email) {
        Optional<Socket> socketOpt = userManagementService.getSocketByEmail(email);
        if (socketOpt.isPresent()) {
            try {
                DataOutputStream out = new DataOutputStream(socketOpt.get().getOutputStream());
                SocketMessage msg = new SocketMessage(EventType.CHAT_CLOSE, null);
                synchronized (out) {
                    out.writeUTF(gson.toJson(msg));
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public static class PendingRequest {
        private final String requesterEmail;
        private final String targetEmail;
        private final long timestamp;

        public PendingRequest(String requesterEmail, String targetEmail, long timestamp) {
            this.requesterEmail = requesterEmail;
            this.targetEmail = targetEmail;
            this.timestamp = timestamp;
        }

        public String getRequesterEmail() {
            return requesterEmail;
        }

        public String getTargetEmail() {
            return targetEmail;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
