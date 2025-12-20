package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.infustructre.adaptors.LogRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileLogRepository implements LogRepository {

    private final Path logFilePath;
    private final Object writeLock = new Object();

    public FileLogRepository() {
        this.logFilePath = Paths.get(Config.LOG_FILE_PATH);
    }

    @Override
    public void info(String message) {
        try {
            log("INFO", message);
        } catch (Exception e) {
            System.err.println("CRITICAL: Logger failed during INFO operation. " + e.getMessage());
        }
    }

    @Override
    public void error(Error error) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            log("ERROR", sw.toString());
        } catch (Exception e) {
            System.err.println("CRITICAL: Logger failed during ERROR operation. " + e.getMessage());
        }
    }

    @Override
    public List<String> getLogs() {
        try {
            ensureFileAndDirectoryExists();
            try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
                return reader.lines().collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: Logger failed during GETLOGS operation. " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void log(String level, String message) throws IOException {
        ensureFileAndDirectoryExists();
        String logEntry = "[" + Instant.now().toString() + "] [" + level + "] " + message;

        if ("ERROR".equals(level)) {
            System.err.println(logEntry);
        } else {
            System.out.println(logEntry);
        }

        synchronized (writeLock) {
            File file = new File("logs.txt");
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new FileWriter(file, true));// true is to append to the file that way we dont
                                                                     // remove exisitng logs
                writer.println(logEntry);
            } catch (IOException e) {
                throw new RuntimeException("cant write to file get error ", e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    private void ensureFileAndDirectoryExists() throws IOException {
        Files.createDirectories(this.logFilePath.getParent());
        if (!Files.exists(this.logFilePath)) {
            Files.createFile(this.logFilePath);
        }
    }
}
