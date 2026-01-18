package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.LogEntry;
import server.infustructre.adaptors.LogRepository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileLogRepository implements LogRepository {

    private final File logFile;
    private final Object writeLock = new Object();

    public FileLogRepository() {
        this.logFile = new File(Config.LOGS_DIR, "logs.txt");
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

        if (!logFile.exists()) {
            return logs;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(logFile);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                LogEntry entry = decode(line);
                
                if (entry != null) {
                    logs.add(entry);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("cant read file error " + logFile.getAbsolutePath(), e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
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
                type = LogEntry.LogType.AUTHENTICATION;
            }
            
            String email = parts[3];
            String message = parts[4];

            return new LogEntry(email, type, message, level, timestamp);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeToFile(String line) {
        if (line == null) {
            throw new IllegalArgumentException("cant write null entity");
        }

        synchronized (writeLock) {
            ensureFileExists();

            PrintWriter writer = null;

            try {
                writer = new PrintWriter(new FileWriter(logFile, true));
                writer.println(line);
            } catch (IOException e) {
                throw new RuntimeException("Unable to write to file. Get error: ", e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    private void ensureFileExists() {
        try {
            File parentDir = logFile.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
