package server.chat;

import com.google.gson.Gson;
import server.application.adaptors.UserManagementService;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;
import server.infustructre.adaptors.EmployeeRepository;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

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

    public synchronized String requestBranchChat(String requesterEmail, UUID targetBranchId) {
        List<Employee> branchEmployees = employeeRepository.findByBranchId(targetBranchId);

        String selectedEmail = null;
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

            if (pendingRequests.containsKey(email)) {
                continue;
            }

            selectedEmail = email;
            break;
        }

        if (selectedEmail == null) {
            sendSystemMessage(requesterEmail, "No employees available in that branch. Try again later.");
            sendCloseConfirmation(requesterEmail); // Close client's chat mode
            return null;
        }

        PendingRequest request = new PendingRequest(requesterEmail, selectedEmail, System.currentTimeMillis());
        pendingRequests.put(selectedEmail, request);

        sendChatRequest(selectedEmail, requesterEmail);
        sendSystemMessage(requesterEmail, "Request sent. Waiting for " + selectedEmail + " to accept...");

        return selectedEmail;
    }

    public synchronized boolean acceptChat(String accepterEmail) {
        PendingRequest request = pendingRequests.remove(accepterEmail);
        if (request == null) {
            sendSystemMessage(accepterEmail, "No pending chat request found.");
            return false;
        }

        String requesterEmail = request.getRequesterEmail();

        activeChats.put(requesterEmail, accepterEmail);
        activeChats.put(accepterEmail, requesterEmail);

        sendSystemMessage(requesterEmail, "Chat accepted! You are now connected to " + accepterEmail);
        sendSystemMessage(accepterEmail, "You are now connected to " + requesterEmail);

        System.out.println("[ChatManager] Chat established: " + requesterEmail + " <-> " + accepterEmail);
        return true;
    }

    public synchronized void declineChat(String declinerEmail) {
        PendingRequest request = pendingRequests.remove(declinerEmail);
        if (request != null) {
            String requesterEmail = request.getRequesterEmail();
            sendSystemMessage(requesterEmail, "Chat request was declined. Try again later.");
        }
    }

    public PendingRequest getPendingRequest(String email) {
        return pendingRequests.get(email);
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
        String partnerEmail = activeChats.remove(email);
        if (partnerEmail != null) {
            activeChats.remove(partnerEmail);
            sendSystemMessage(partnerEmail, "The other user has left the chat.");
            System.out.println("[ChatManager] Chat closed: " + email + " <-> " + partnerEmail);
        }

        pendingRequests.remove(email);
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

        public boolean isExpired(long timeoutMs) {
            return System.currentTimeMillis() - timestamp > timeoutMs;
        }
    }
}
