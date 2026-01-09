package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.LogEntry;
import server.infustructre.adaptors.LogRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileLogRepository implements LogRepository {

    private final Path logFilePath;
    private final Object writeLock = new Object();

    public FileLogRepository() {
        this.logFilePath = Paths.get(Config.LOGS_DIR, "logs.txt");
        ensureFileExists();
    }

    @Override
    public void save(LogEntry entry) {
        String line = encode(entry);
        writeToFile(line);
    }

    @Override
    public List<LogEntry> findAll() {
        List<LogEntry> logs = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogEntry entry = decode(line);
                if (entry != null) {
                    logs.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read logs: " + e.getMessage());
        }
        return logs;
    }
    
    @Override
    public void info(LogEntry.LogType type, String message) {
        info(type, "SYSTEM", message);
    }
    @Override
    public void info(LogEntry.LogType type, String userId, String message) {
        LogEntry entry = new LogEntry(userId, type, message, LogEntry.LogLevel.INFO);
        save(entry);
        System.out.println("[INFO] [" + type + "] " + message);
    }

    @Override
    public void error(LogEntry.LogType type, String message) {
        error(type, "SYSTEM", message);
    }

    @Override
    public void error(LogEntry.LogType type, String userId, String message) {
        LogEntry entry = new LogEntry(userId, type, message, LogEntry.LogLevel.ERROR);
        save(entry);
        System.err.println("[ERROR] [" + type + "] " + message);
    }


    private String encode(LogEntry log) {
        return log.getTimestamp().toString() + "|" +
               log.getLevel() + "|" +
               log.getType() + "|" +
               log.getEmail() + "|" +
               log.getMessage();
    }

    private LogEntry decode(String line) {
        try {
            String[] parts = line.split("\\|", 5);
            if (parts.length < 5) return null;

            LocalDateTime timestamp = LocalDateTime.parse(parts[0]);
            LogEntry.LogLevel level = LogEntry.LogLevel.valueOf(parts[1]);
            
            LogEntry.LogType type;
            try {
                type = LogEntry.LogType.valueOf(parts[2]);
            } catch (Exception e) {
                type = LogEntry.LogType.LOGIN;
            }
            
            String email = parts[3];
            String message = parts[4];

            return new LogEntry(email, type, message, level, timestamp);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeToFile(String line) {
        synchronized (writeLock) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))) {
                writer.println(line);
            } catch (IOException e) {
                System.err.println("Failed to write log: " + e.getMessage());
            }
        }
    }

    private void ensureFileExists() {
        try {
            if (logFilePath.getParent() != null) {
                Files.createDirectories(logFilePath.getParent());
            }
            if (!Files.exists(logFilePath)) {
                Files.createFile(logFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
