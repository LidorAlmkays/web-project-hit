package server.chat;

import com.google.gson.Gson;
import server.application.adaptors.UserManagementService;
import server.domain.LogEntry;
import server.domain.chat.ChatParticipant;
import server.domain.chat.ChatSession;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;
import shareddto.chat.PendingRequestInfo;
import shareddto.reporting.ChatHistoryDto;
import shareddto.reporting.ChatHistoryEntryDto;
import shareddto.reporting.ChatSessionDto;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChatManager {
    private static final String CHAT_LOG_DIR = "data/chats";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static ChatManager instance;
    private final Gson gson = new Gson();
    private final Map<String, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final Map<String, String> userToSession = new ConcurrentHashMap<>();
    private final Map<String, List<PendingRequest>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> activeRequests = new ConcurrentHashMap<>();
    private UserManagementService userManagementService;
    private EmployeeRepository employeeRepository;
    private LogRepository logRepository;

    private ChatManager() {
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void setDependencies(UserManagementService userManagementService, EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        this.userManagementService = userManagementService;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
    }

    public synchronized void requestBranchChat(String requesterEmail, UUID targetBranchId) {
        List<Employee> branchEmployees = employeeRepository.findByBranchId(targetBranchId);
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

        activeRequests.put(requesterEmail, ConcurrentHashMap.newKeySet());
        activeRequests.get(requesterEmail).addAll(availableEmails);

        for (String email : availableEmails) {
            PendingRequest request = new PendingRequest(requesterEmail, email, System.currentTimeMillis());
            pendingRequests.computeIfAbsent(email, k -> new ArrayList<>()).add(request);
        }

        sendSystemMessage(requesterEmail,
                "Request sent to " + availableEmails.size() + " employee(s). Waiting for someone to accept...");
    }

    public synchronized void acceptChat(String accepterEmail, String targetRequesterEmail) {
        List<PendingRequest> queue = pendingRequests.get(accepterEmail);
        if (queue == null || queue.isEmpty()) {
            sendSystemMessage(accepterEmail, "No pending chat requests found.");
        }

        PendingRequest request = null;
        assert queue != null;
        for (PendingRequest r : queue) {
            if (r.getRequesterEmail().equals(targetRequesterEmail)) {
                request = r;
                break;
            }
        }

        if (request == null) {
            sendSystemMessage(accepterEmail, "Request from " + targetRequesterEmail + " no longer exists.");
        }

        queue.remove(request);
        if (queue.isEmpty()) {
            pendingRequests.remove(accepterEmail);
        }

        String requesterEmail = request.getRequesterEmail();

        activeRequests.remove(requesterEmail);
        pendingRequests.values().forEach(list -> list.removeIf(r -> r.getRequesterEmail().equals(requesterEmail)));
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(sessionId);

        UUID requesterBranchId = getBranchIdForEmail(requesterEmail);
        UUID accepterBranchId = getBranchIdForEmail(accepterEmail);

        session.addParticipant(new ChatParticipant(requesterEmail, requesterBranchId, false));
        session.addParticipant(new ChatParticipant(accepterEmail, accepterBranchId, false));

        chatSessions.put(sessionId, session);
        userToSession.put(requesterEmail, sessionId);
        userToSession.put(accepterEmail, sessionId);

        sendSystemMessage(requesterEmail, "Chat accepted! You are now connected to " + accepterEmail);
        sendSystemMessage(accepterEmail, "You are now connected to " + requesterEmail);
        startChatLog(session, requesterEmail, accepterEmail);

        logRepository.info(LogEntry.LogType.CHAT, "[ChatManager] Chat established: " + requesterEmail + " <-> " + accepterEmail);
    }

    public synchronized void declineChat(String declinerEmail, String targetRequesterEmail) {
        List<PendingRequest> queue = pendingRequests.get(declinerEmail);
        if (queue != null && !queue.isEmpty()) {
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

                Set<String> targets = activeRequests.get(requesterEmail);
                if (targets != null) {
                    targets.remove(declinerEmail);

                    if (targets.isEmpty()) {
                        activeRequests.remove(requesterEmail);
                        sendSystemMessage(requesterEmail, "All available employees declined your request.");
                        sendCloseConfirmation(requesterEmail);
                    }
                }
            }
        }
    }

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

    public List<ChatSession> getActiveChats(String managerEmail, UUID managerBranchId, boolean isAdmin) {
        List<ChatSession> result = new ArrayList<>();
        for (ChatSession session : chatSessions.values()) {
            if (isAdmin) {
                result.add(session);
            } else {
                boolean involvesMyBranch = session.getParticipants().stream()
                        .anyMatch(p -> managerBranchId.equals(p.getBranchId()));
                if (involvesMyBranch) {
                    result.add(session);
                }
            }
        }
        return result;
    }

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

        for (String participantEmail : session.getParticipantEmails()) {
            if (!participantEmail.equals(managerEmail)) {
                sendSystemMessage(participantEmail, "Manager " + managerEmail + " has joined the chat.");
            }
        }

        sendChatHistory(managerEmail, session);

        sendSystemMessage(managerEmail, "You have joined the chat.");
        logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Manager " + managerEmail + " joined session " + sessionId);
    }

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
                logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Failed to read chat history: " + e.getMessage());
            }
        }
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

        logMessage(session, senderEmail, message);

        for (String participantEmail : session.getParticipantEmails()) {
            if (!participantEmail.equals(senderEmail)) {
                sendChatMessage(participantEmail, senderEmail, message);
            }
        }
    }

    public synchronized void closeChat(String email) {
        String sessionId = userToSession.remove(email);
        if (sessionId != null) {
            ChatSession session = chatSessions.get(sessionId);
            if (session != null) {
                session.removeParticipant(email);

                Set<String> remainingParticipants = new java.util.HashSet<>(session.getParticipantEmails());

                for (String participantEmail : remainingParticipants) {
                    sendSystemMessage(participantEmail, email + " has left the chat.");
                }

                if (!session.shouldRemainOpen()) {
                    closeChatLog(session);
                    for (String participantEmail : remainingParticipants) {
                        userToSession.remove(participantEmail);
                        sendCloseConfirmation(participantEmail);
                    }
                    chatSessions.remove(sessionId);
                    logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Chat session " + sessionId + " closed.");
                }
            }
        }

        pendingRequests.remove(email);

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
            logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Cancelled request from " + email);
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
                out.writeUTF(gson.toJson(msg));
                out.flush();
            } catch (IOException e) {
                logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Failed to send message: " + e.getMessage());
            }
        }
    }

    private void sendCloseConfirmation(String email) {
        Optional<Socket> socketOpt = userManagementService.getSocketByEmail(email);
        if (socketOpt.isPresent()) {
            try {
                DataOutputStream out = new DataOutputStream(socketOpt.get().getOutputStream());
                SocketMessage msg = new SocketMessage(EventType.CHAT_CLOSE, null);
                out.writeUTF(gson.toJson(msg));
                out.flush();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private void startChatLog(ChatSession session, String email1, String email2) {
        try {
            File dir = new File(CHAT_LOG_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Cannot create chat log directory: " + CHAT_LOG_DIR);
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

            logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Chat log started: " + filename);
        } catch (IOException e) {
            logRepository.info(LogEntry.LogType.CHAT,"[ChatManager] Failed to create chat log: " + e.getMessage());
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

    public ChatHistoryDto getChatHistory() {
        logRepository.info(LogEntry.LogType.CHAT, "[ChatManager] Admin requested chat history export");
        
        List<ChatSessionDto> chatSessions = new ArrayList<>();
        File chatDir = new File(CHAT_LOG_DIR);
        
        if (!chatDir.exists() || !chatDir.isDirectory()) {
            return new ChatHistoryDto(
                LocalDateTime.now().toString(),
                0,
                chatSessions
            );
        }

        File[] chatFiles = chatDir.listFiles((dir, name) -> name.startsWith("chat_") && name.endsWith(".txt"));
        
        if (chatFiles == null) {
            return new ChatHistoryDto(
                LocalDateTime.now().toString(),
                0,
                chatSessions
            );
        }

        for (File chatFile : chatFiles) {
            try {
                ChatSessionDto session = parseChatFile(chatFile);
                if (session != null) {
                    chatSessions.add(session);
                }
            } catch (Exception e) {
                logRepository.error(LogEntry.LogType.CHAT, 
                    "[ChatManager] Failed to parse chat file " + chatFile.getName() + ": " + e.getMessage());
            }
        }

        return new ChatHistoryDto(
            LocalDateTime.now().toString(),
            chatSessions.size(),
            chatSessions
        );
    }

    private ChatSessionDto parseChatFile(File chatFile) throws IOException {
        List<String> lines = Files.readAllLines(chatFile.toPath());
        if (lines.isEmpty()) {
            return null;
        }

        String fileName = chatFile.getName();
        String startTime = null;
        String endTime = null;
        List<String> participants = new ArrayList<>();
        List<ChatHistoryEntryDto> messages = new ArrayList<>();

        // Parse header: "=== Chat started at yyyy-MM-dd HH:mm:ss ==="
        Pattern startPattern = Pattern.compile("=== Chat started at (.+) ===");
        // Parse participants: "Participants: email1 <-> email2"
        Pattern participantsPattern = Pattern.compile("Participants: (.+) <-> (.+)");
        // Parse messages: "[yyyy-MM-dd HH:mm:ss] senderEmail: message"
        Pattern messagePattern = Pattern.compile("\\[(.+)\\] (.+?): (.+)");
        // Parse footer: "=== Chat ended at yyyy-MM-dd HH:mm:ss ==="
        Pattern endPattern = Pattern.compile("=== Chat ended at (.+) ===");

        for (String line : lines) {
            Matcher startMatcher = startPattern.matcher(line);
            if (startMatcher.matches()) {
                startTime = startMatcher.group(1);
                continue;
            }

            Matcher participantsMatcher = participantsPattern.matcher(line);
            if (participantsMatcher.matches()) {
                participants.add(participantsMatcher.group(1).trim());
                participants.add(participantsMatcher.group(2).trim());
                continue;
            }

            Matcher endMatcher = endPattern.matcher(line);
            if (endMatcher.matches()) {
                endTime = endMatcher.group(1);
                continue;
            }

            Matcher messageMatcher = messagePattern.matcher(line);
            if (messageMatcher.matches()) {
                String timestamp = messageMatcher.group(1);
                String senderEmail = messageMatcher.group(2);
                String message = messageMatcher.group(3);
                messages.add(new ChatHistoryEntryDto(timestamp, senderEmail, message));
            }
        }

        return new ChatSessionDto(
            fileName,
            startTime != null ? startTime : "Unknown",
            endTime != null ? endTime : "Ongoing",
            participants,
            messages
        );
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

        public long getTimestamp() {
            return timestamp;
        }
    }
}
