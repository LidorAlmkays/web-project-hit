package server.domain;

import java.time.LocalDateTime;

public class LogEntry {
    
    public enum LogType {
        LOGIN,
        REGISTER,
        PURCHASE,
        CHAT_START,
        ERROR
    }

    public enum LogLevel {
        INFO,
        WARNING,
        ERROR
    }
    private final String email;
    private final LogType type;
    private final String message;
    private final LocalDateTime timestamp;
    private final LogLevel level;

    public LogEntry(String email, LogType type, String message, LogLevel level) {
        this(email, type, message, level, LocalDateTime.now());
    }

    public LogEntry(String email, LogType type, String message, LogLevel level, LocalDateTime timestamp) {
        this.email = email;
        this.type = type;
        this.message = message;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getEmail() { return email; }
    public LogType getType() { return type; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LogLevel getLevel() { return level; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + level + " | " + type + " | User: " + email + " | " + message;
    }
}