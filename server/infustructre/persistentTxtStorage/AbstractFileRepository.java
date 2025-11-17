package server.infustructre.persistentTxtStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class AbstractFileRepository<T> {
    protected final String rootPath;
    protected final File rootDirectory;

    public AbstractFileRepository(String rootPath) {
        this.rootPath = rootPath;
        this.rootDirectory = new File(rootPath);
        ensureDirectoryExists();
    }

    protected void ensureDirectoryExists() {
        if (!rootDirectory.exists()) {
            boolean created = rootDirectory.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create data directory: " + rootPath);
            }
        } else if (!rootDirectory.isDirectory()) {
            throw new RuntimeException("Path exists but is not a directory: " + rootPath);
        }
    }

    protected void writeToFile(T entity, String fileName) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name must not be null or empty");
        }

        ensureDirectoryExists();

        File file = getFilePath(fileName);
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file);
            String content = encode(entity);
            writer.print(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + file.getAbsolutePath(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    protected File getFilePath(String fileName) {
        String fileNameWithExtension = fileName + ".txt";
        return new File(rootDirectory, fileNameWithExtension);
    }

    protected boolean fileExists(String fileName) {
        File file = getFilePath(fileName);
        return file.exists();
    }

    protected void deleteFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name must not be null or empty");
        }

        File file = getFilePath(fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }

    protected T readFromFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name must not be null or empty");
        }

        File file = getFilePath(fileName);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

            StringBuilder content = new StringBuilder();
            boolean firstLine = true;
            while (scanner.hasNextLine()) {
                if (!firstLine) {
                    content.append("\n");
                }
                content.append(scanner.nextLine());
                firstLine = false;
            }

            String fileContent = content.toString();
            return decodeFromString(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to read from file: " + file.getAbsolutePath(), e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    protected List<T> readAllFromDirectory() {
        List<T> entities = new ArrayList<>();

        ensureDirectoryExists();

        File[] files = rootDirectory.listFiles();
        if (files == null) {
            return entities;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try {
                    String fileName = file.getName();
                    String baseFileName = fileName.substring(0, fileName.length() - 4);// remove the .txt from file
                                                                                       // name, thats why -4.
                    T entity = readFromFile(baseFileName);
                    entities.add(entity);
                } catch (RuntimeException e) {
                    System.err.println("Failed to read file " + file.getName() + ": " + e.getMessage());// i dont want
                                                                                                        // to stop the
                                                                                                        // program if
                                                                                                        // one of the
                                                                                                        // files is not
                                                                                                        // read
                                                                                                        // currectly so
                                                                                                        // i print for
                                                                                                        // me to debug
                                                                                                        // as the
                                                                                                        // project
                                                                                                        // mantainer to
                                                                                                        // fix.
                }
            }
        }

        return entities;
    }

    protected abstract String encode(T entity);

    protected abstract T decodeFromString(String content);
}
