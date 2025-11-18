package server.infustructre.persistentTxtStorage;

import server.domain.ChatRoomMessageStore;
import server.infustructre.adaptors.ChatRoomMessageRepository;
import server.config.Config;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileChatRoomMessageRepository extends AbstractFileRepository<ChatRoomMessageStore>
        implements ChatRoomMessageRepository {
    private final Map<String, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();

    public FileChatRoomMessageRepository() {
        super(Config.getChatRoomsDir());
    }

    private Object getLock(String chatRoomId) {
        Object lock = locks.get(chatRoomId);
        if (lock == null) {
            synchronized (creationMutex) {// this is for when 2 threads try to get the lock at the same TIME for an item
                // that still dosnt exists yet (for safty :3)
                lock = locks.get(chatRoomId);
                if (lock == null) {
                    lock = new Object();
                    locks.put(chatRoomId, lock);
                }
            }
        }
        return lock;
    }

    @Override
    protected String encode(ChatRoomMessageStore entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getChatRoomId()).append("\n");

        List<String> messageHistory = entity.getMessageHistory();
        for (String message : messageHistory) {
            sb.append(message).append("\n");
        }

        return sb.toString();
    }

    @Override
    protected ChatRoomMessageStore decodeFromString(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Invalid chat room format: content is empty");
        }

        String[] lines = content.split("\n");

        if (lines.length < 1) {
            throw new RuntimeException("Invalid chat room format: insufficient data");
        }

        UUID chatRoomId = UUID.fromString(lines[0].trim());

        List<String> messageHistory = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                messageHistory.add(lines[i]);
            }
        }

        return new ChatRoomMessageStore(chatRoomId, messageHistory);
    }

    @Override
    public Optional<ChatRoomMessageStore> findById(UUID chatRoomId) {
        String chatRoomIdStr = chatRoomId.toString();
        Object lock = getLock(chatRoomIdStr);
        synchronized (lock) {
            String fileName = chatRoomIdStr;
            if (!fileExists(fileName)) {
                return Optional.empty();
            }
            try {
                ChatRoomMessageStore messageStore = readFromFile(fileName);
                return Optional.of(messageStore);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }
    }

    @Override
    public List<ChatRoomMessageStore> findAll() {
        return readAllFromDirectory();
    }

    @Override
    public void save(ChatRoomMessageStore messageStore) {
        String chatRoomId = messageStore.getChatRoomId().toString();
        Object lock = getLock(chatRoomId);
        synchronized (lock) {
            String fileName = messageStore.getChatRoomId().toString();
            writeToFile(messageStore, fileName);
        }
    }

    @Override
    public void deleteById(UUID chatRoomId) {
        String fileName = chatRoomId.toString();
        deleteFile(fileName);
    }

    @Override
    public boolean existsById(UUID chatRoomId) {
        String chatRoomIdStr = chatRoomId.toString();
        Object lock = getLock(chatRoomIdStr);
        synchronized (lock) {
            String fileName = chatRoomIdStr;
            return fileExists(fileName);
        }
    }
}
