package server.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import server.config.Config;

public class Logger {
    private static final Logger instance = new Logger();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Object lock = new Object();

    private Logger() {
        ensureLogDirectoryExists();
    }

    private static Logger getInstance() {
        return instance;
    }

    private void ensureLogDirectoryExists() {
        File logFile = new File(Config.getLogFilePath());
        File logDirectory = logFile.getParentFile();
        if (logDirectory != null && !logDirectory.exists()) {
            boolean created = logDirectory.mkdirs();
            if (!created) {
                System.err.println("Failed to create log directory: " + logDirectory.getAbsolutePath());
            }
        }
    }

    private void writeLog(String level, String message) {
        if (message == null) {
            message = "null";
        }

        synchronized (lock) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(Config.getLogFilePath(), true))) {
                String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
                String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
                writer.println(logEntry);
                writer.flush();
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }
    }

    public static void logInfo(String message) {
        getInstance().writeLog("INFO", message);
    }

    public static void logError(String message) {
        getInstance().writeLog("ERROR", message);
    }

    public static List<String> getLogs() {
        return getInstance().readAllLogs();
    }

    private List<String> readAllLogs() {
        List<String> logs = new ArrayList<>();
        File logFile = new File(Config.getLogFilePath());

        if (!logFile.exists()) {
            return logs;
        }

        Scanner scanner = null;
        synchronized (lock) {
            try {
                scanner = new Scanner(logFile);
                while (scanner.hasNextLine()) {
                    logs.add(scanner.nextLine());
                }
            } catch (FileNotFoundException e) {
                System.err.println("Failed to read log file: " + e.getMessage());
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }

        return logs;
    }
}
