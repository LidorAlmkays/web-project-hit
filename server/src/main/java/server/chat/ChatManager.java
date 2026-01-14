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

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton state manager for chat sessions between branches.
 * Does not handle socket events directly - that's done by chat handlers.
 */
public class ChatManager {
    private static final String CHAT_LOG_DIR = "data/chats";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static ChatManager instance;
    private final Gson gson = new Gson();
    private final Map<String, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final Map<String, String> userToSession = new ConcurrentHashMap<>();
    private final Map<String, List<PendingRequest>> pendingRequests = new ConcurrentHashMap<>();
    // Map of requesterEmail -> Set of targetEmails (to track who they are waiting
    // for)
    private final Map<String, Set<String>> activeRequests = new ConcurrentHashMap<>();
    private UserManagementService userManagementService;
    private EmployeeRepository employeeRepository;

    private ChatManager() {
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void setDependencies(UserManagementService userManagementService, EmployeeRepository employeeRepository) {
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

            if (userToSession.containsKey(email)) {
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

        // Create new chat session
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(sessionId);

        // Get branch IDs for participants
        UUID requesterBranchId = getBranchIdForEmail(requesterEmail);
        UUID accepterBranchId = getBranchIdForEmail(accepterEmail);

        session.addParticipant(new ChatParticipant(requesterEmail, requesterBranchId, false));
        session.addParticipant(new ChatParticipant(accepterEmail, accepterBranchId, false));

        chatSessions.put(sessionId, session);
        userToSession.put(requesterEmail, sessionId);
        userToSession.put(accepterEmail, sessionId);

        sendSystemMessage(requesterEmail, "Chat accepted! You are now connected to " + accepterEmail);
        sendSystemMessage(accepterEmail, "You are now connected to " + requesterEmail);

        // Start chat log
        startChatLog(session, requesterEmail, accepterEmail);

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
        List<PendingRequest> list = pendingRequests.get(email);
        return list != null ? new ArrayList<>(list) : null;
    }

    /**
     * Get list of pending request info objects for displaying in UI.
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
        return userToSession.containsKey(email);
    }

    /**
     * Get list of active chat sessions for managers.
     * SHIFT_MANAGER sees only their branch chats, ADMIN sees all.
     */
    public List<ChatSession> getActiveChats(String managerEmail, UUID managerBranchId, boolean isAdmin) {
        List<ChatSession> result = new ArrayList<>();
        for (ChatSession session : chatSessions.values()) {
            if (isAdmin) {
                result.add(session);
            } else {
                // SHIFT_MANAGER sees only chats involving their branch
                boolean involvesMyBranch = session.getParticipants().stream()
                        .anyMatch(p -> managerBranchId.equals(p.getBranchId()));
                if (involvesMyBranch) {
                    result.add(session);
                }
            }
        }
        return result;
    }

    /**
     * Manager joins an active chat session.
     */
    public synchronized void joinChat(String managerEmail, String sessionId) {
        ChatSession session = chatSessions.get(sessionId);
        if (session == null) {
            sendSystemMessage(managerEmail, "Chat session not found.");
            return;
        }

        if (userToSession.containsKey(managerEmail)) {
            sendSystemMessage(managerEmail, "You are already in a chat.");
            return;
        }

        UUID managerBranchId = getBranchIdForEmail(managerEmail);
        session.addParticipant(new ChatParticipant(managerEmail, managerBranchId, true));
        userToSession.put(managerEmail, sessionId);

        // Notify existing participants
        for (String participantEmail : session.getParticipantEmails()) {
            if (!participantEmail.equals(managerEmail)) {
                sendSystemMessage(participantEmail, "Manager " + managerEmail + " has joined the chat.");
            }
        }

        // Send chat history to manager
        sendChatHistory(managerEmail, session);

        sendSystemMessage(managerEmail, "You have joined the chat.");
        System.out.println("[ChatManager] Manager " + managerEmail + " joined session " + sessionId);
    }

    /**
     * Send chat history to a newly joined manager.
     */
    private void sendChatHistory(String managerEmail, ChatSession session) {
        String logPath = session.getLogFilePath();
        if (logPath != null) {
            try {
                java.nio.file.Path path = java.nio.file.Paths.get(logPath);
                if (java.nio.file.Files.exists(path)) {
                    String history = java.nio.file.Files.readString(path);
                    sendSystemMessage(managerEmail, "=== Chat History ===\n" + history + "\n=== End History ===");
                }
            } catch (IOException e) {
                System.err.println("[ChatManager] Failed to read chat history: " + e.getMessage());
            }
        }
    }

    public ChatSession getSession(String sessionId) {
        return chatSessions.get(sessionId);
    }

    public Set<String> getChatPartners(String email) {
        String sessionId = userToSession.get(email);
        if (sessionId == null)
            return Set.of();
        ChatSession session = chatSessions.get(sessionId);
        if (session == null)
            return Set.of();
        Set<String> partners = new java.util.HashSet<>(session.getParticipantEmails());
        partners.remove(email);
        return partners;
    }

    public synchronized void handleMessage(String senderEmail, String message) {
        String sessionId = userToSession.get(senderEmail);
        if (sessionId == null) {
            sendSystemMessage(senderEmail, "You are not in an active chat.");
            return;
        }

        ChatSession session = chatSessions.get(sessionId);
        if (session == null) {
            sendSystemMessage(senderEmail, "You are not in an active chat.");
            return;
        }

        // Log message
        logMessage(session, senderEmail, message);

        // Send to all other participants
        for (String participantEmail : session.getParticipantEmails()) {
            if (!participantEmail.equals(senderEmail)) {
                sendChatMessage(participantEmail, senderEmail, message);
            }
        }
    }

    public synchronized void closeChat(String email) {
        // 1. Handle active chat cleanup
        String sessionId = userToSession.remove(email);
        if (sessionId != null) {
            ChatSession session = chatSessions.get(sessionId);
            if (session != null) {
                session.removeParticipant(email);

                // Copy to avoid ConcurrentModificationException
                Set<String> remainingParticipants = new java.util.HashSet<>(session.getParticipantEmails());

                // Notify remaining participants
                for (String participantEmail : remainingParticipants) {
                    sendSystemMessage(participantEmail, email + " has left the chat.");
                }

                // Check if chat should remain open (2+ participants from different branches)
                if (!session.shouldRemainOpen()) {
                    // Close the entire session
                    closeChatLog(session);
                    for (String participantEmail : remainingParticipants) {
                        userToSession.remove(participantEmail);
                        sendCloseConfirmation(participantEmail);
                    }
                    chatSessions.remove(sessionId);
                    System.out.println("[ChatManager] Chat session " + sessionId + " closed.");
                }
            }
        }

        // 2. Handle pending request cleanup (if this user was a target)
        pendingRequests.remove(email);

        // 3. Handle active request cleanup (if this user was a requester waiting)
        Set<String> targets = activeRequests.remove(email);
        if (targets != null) {
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

    // ==================== Chat Logging ====================

    private void startChatLog(ChatSession session, String email1, String email2) {
        try {
            File dir = new File(CHAT_LOG_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("[ChatManager] Cannot create chat log directory: " + CHAT_LOG_DIR);
                return;
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = CHAT_LOG_DIR + "/chat_" + timestamp + "_" + sanitize(email1) + "_" + sanitize(email2)
                    + ".txt";

            PrintWriter writer = new PrintWriter(new FileWriter(filename, true));
            writer.println("=== Chat started at " + LocalDateTime.now().format(TIME_FORMAT) + " ===");
            writer.println("Participants: " + email1 + " <-> " + email2);
            writer.println();
            writer.flush();

            session.setLogWriter(writer);
            session.setLogFilePath(filename);

            System.out.println("[ChatManager] Chat log started: " + filename);
        } catch (IOException e) {
            System.err.println("[ChatManager] Failed to create chat log: " + e.getMessage());
        }
    }

    private void logMessage(ChatSession session, String senderEmail, String message) {
        PrintWriter writer = session.getLogWriter();
        if (writer != null) {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            writer.println("[" + timestamp + "] " + senderEmail + ": " + message);
            writer.flush();
        }
    }

    private void closeChatLog(ChatSession session) {
        PrintWriter writer = session.getLogWriter();
        if (writer != null) {
            writer.println();
            writer.println("=== Chat ended at " + LocalDateTime.now().format(TIME_FORMAT) + " ===");
            writer.close();
            session.setLogWriter(null);
        }
    }

    private UUID getBranchIdForEmail(String email) {
        return employeeRepository.findByEmail(email).map(Employee::getBranchId).orElse(null);
    }

    private String sanitize(String email) {
        return email.replaceAll("[^a-zA-Z0-9]", "_");
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
