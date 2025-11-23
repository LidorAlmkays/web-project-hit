package server.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import server.config.Config;

public class Logger {
    private static final Logger instance = new Logger();
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
        synchronized (lock) {
            File logFile = new File(Config.getLogFilePath());
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(logFile);
                String logEntry = "[" + level + "] " + message;
                writer.println(logEntry);
                writer.flush();
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    public static void info(String message) {
        getInstance().writeLog("INFO", message);
    }

    public static void error(String message) {
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
