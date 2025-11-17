package server.infustructre.persistentTxtStorage;

import server.domain.UserAccount;
import server.domain.UserRole;
import server.infustructre.adaptors.UserAccountRepository;
import server.config.Config;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileUserAccountRepository extends AbstractFileRepository<UserAccount> implements UserAccountRepository {
    private final Map<String, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();

    public FileUserAccountRepository() {
        super(Config.getUserAccountsDir());
    }

    private Object getLock(String emailAddress) {
        String sanitized = sanitizeFileName(emailAddress);
        Object lock = locks.get(sanitized);
        if (lock == null) {
            synchronized (creationMutex) {// this is for when 2 threads try to get the lock at the same TIME for an item
                // that still dosnt exists yet (for safty :3)
                lock = locks.get(sanitized);
                if (lock == null) {
                    lock = new Object();
                    locks.put(sanitized, lock);
                }
            }
        }
        return lock;
    }

    private String sanitizeFileName(String email) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < email.length(); i++) {
            char c = email.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '@') {
                result.append(c);
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    @Override
    protected String encode(UserAccount entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getUsername()).append("\n");
        sb.append(entity.getEmailAddress()).append("\n");
        sb.append(entity.getPassword()).append("\n");
        sb.append(entity.getRole()).append("\n");
        return sb.toString();
    }

    @Override
    protected UserAccount decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 4) {
            throw new RuntimeException("Invalid user account format: insufficient data");
        }

        String username = lines[0].trim();
        String emailAddress = lines[1].trim();
        String password = lines[2].trim();
        UserRole role = UserRole.valueOf(lines[3].trim());

        return new UserAccount(username, emailAddress, password, role);
    }

    @Override
    public Optional<UserAccount> findByEmailAddress(String emailAddress) {
        Object lock = getLock(emailAddress);
        synchronized (lock) {
            String fileName = sanitizeFileName(emailAddress);
            if (!fileExists(fileName)) {
                return Optional.empty();
            }
            try {
                UserAccount account = readFromFile(fileName);
                return Optional.of(account);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }
    }

    @Override
    public void save(UserAccount userAccount) {
        String emailAddress = userAccount.getEmailAddress();
        Object lock = getLock(emailAddress);
        synchronized (lock) {
            String fileName = sanitizeFileName(emailAddress);
            if (existsByEmailAddress(emailAddress)) {
                throw new IllegalArgumentException("Email address already exists: " + emailAddress);
            }
            writeToFile(userAccount, fileName);
        }
    }

    @Override
    public void updateUserByEmailAddress(String emailAddress, UserAccount userAccount) {
        Object lock = getLock(emailAddress);
        synchronized (lock) {
            if (!existsByEmailAddress(emailAddress)) {
                throw new IllegalArgumentException("User account does not exist: " + emailAddress);
            }
            String newEmail = userAccount.getEmailAddress();
            if (!emailAddress.equals(newEmail)) {
                if (existsByEmailAddress(newEmail)) {
                    throw new IllegalArgumentException("Email address already exists: " + newEmail);
                }
                String oldFileName = sanitizeFileName(emailAddress);
                deleteFile(oldFileName);
            }
            String fileName = sanitizeFileName(newEmail);
            writeToFile(userAccount, fileName);
        }
    }

    @Override
    public void deleteByEmailAddress(String emailAddress) {
        String fileName = sanitizeFileName(emailAddress);
        deleteFile(fileName);
    }

    @Override
    public boolean existsByEmailAddress(String emailAddress) {
        Object lock = getLock(emailAddress);
        synchronized (lock) {
            String fileName = sanitizeFileName(emailAddress);
            return fileExists(fileName);
        }
    }
}
