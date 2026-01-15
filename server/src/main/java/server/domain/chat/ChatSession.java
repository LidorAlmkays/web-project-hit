package server.domain.chat;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatSession {
    private final String sessionId;
    private final Set<ChatParticipant> participants = new HashSet<>();
    private PrintWriter logWriter;
    private final long startTime;
    private String logFilePath;

    public ChatSession(String sessionId) {
        this.sessionId = sessionId;
        this.startTime = System.currentTimeMillis();
    }

    public String getSessionId() {
        return sessionId;
    }

    public Set<ChatParticipant> getParticipants() {
        return participants;
    }

    public void addParticipant(ChatParticipant participant) {
        participants.add(participant);
    }

    public void removeParticipant(String email) {
        participants.removeIf(p -> p.getEmail().equals(email));
    }

    public Set<String> getParticipantEmails() {
        return participants.stream()
                .map(ChatParticipant::getEmail)
                .collect(Collectors.toSet());
    }

    public PrintWriter getLogWriter() {
        return logWriter;
    }

    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean shouldRemainOpen() {
        Set<UUID> uniqueBranches = participants.stream()
                .map(ChatParticipant::getBranchId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        return uniqueBranches.size() >= 2;
    }

    public String getDisplayString() {
        String emails = participants.stream()
                .map(ChatParticipant::getEmail)
                .collect(Collectors.joining(" <-> "));
        long durationMs = System.currentTimeMillis() - startTime;
        long minutes = durationMs / (1000 * 60);
        return emails + " (" + minutes + "m)";
    }
}
