package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.PasswordSettings;
import server.infustructre.adaptors.PasswordSettingsRepository;

public class FilePasswordSettingsRepository extends AbstractFileRepository<PasswordSettings>
        implements PasswordSettingsRepository {
    private static final String SETTINGS_FILE_NAME = "passwordsSettings";
    private final Object lock = new Object();
    private PasswordSettings cache;

    public FilePasswordSettingsRepository() {
        super(Config.PASSWORD_SETTINGS_DIR);
        loadCache();
    }

    private void loadCache() {
        synchronized (lock) {
            if (fileExists(SETTINGS_FILE_NAME)) {
                try {
                    cache = readFromFile(SETTINGS_FILE_NAME);
                } catch (RuntimeException e) {
                    // If file exists but can't be read, create default settings
                    cache = createDefaultSettings();
                    saveToFile(cache);
                }
            } else {
                // File doesn't exist, create default settings
                cache = createDefaultSettings();
                saveToFile(cache);
            }
        }
    }

    private PasswordSettings createDefaultSettings() {
        return new PasswordSettings(false, false, false);
    }

    private void saveToFile(PasswordSettings settings) {
        writeToFile(settings, SETTINGS_FILE_NAME);
    }

    @Override
    protected String encode(PasswordSettings entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.isPasswordlength8()).append("\n");
        sb.append(entity.isOneUpperletter()).append("\n");
        sb.append(entity.isOneNumber()).append("\n");
        return sb.toString();
    }

    @Override
    protected PasswordSettings decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 3) {
            throw new RuntimeException("Invalid password settings format: insufficient data");
        }

        boolean passwordlength8 = Boolean.parseBoolean(lines[0].trim());
        boolean oneUpperletter = Boolean.parseBoolean(lines[1].trim());
        boolean oneNumber = Boolean.parseBoolean(lines[2].trim());

        return new PasswordSettings(passwordlength8, oneUpperletter, oneNumber);
    }

    @Override
    public void save(PasswordSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("cant save, password settings is null");
        }
        synchronized (lock) {
            saveToFile(settings);
            cache = settings.createCopy();
        }
    }

    @Override
    public PasswordSettings load() {
        synchronized (lock) {
            // Always reload from file to ensure we have the latest data
            if (fileExists(SETTINGS_FILE_NAME)) {
                try {
                    cache = readFromFile(SETTINGS_FILE_NAME);
                } catch (RuntimeException e) {
                    // If read fails, return cached value or default
                    if (cache == null) {
                        cache = createDefaultSettings();
                    }
                }
            } else {
                // File doesn't exist, return cached value or default
                if (cache == null) {
                    cache = createDefaultSettings();
                }
            }
            return cache.createCopy();
        }
    }
}
