package server.infustructre.persistentTxtStorage;

import server.domain.ChatRoom;
import server.infustructre.adaptors.ChatRoomRepository;
import server.config.Config;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileChatRoomRepository extends AbstractFileRepository<ChatRoom> implements ChatRoomRepository {
    private final Map<String, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();

    public FileChatRoomRepository() {
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
    protected String encode(ChatRoom entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getChatRoomId()).append("\n");

        List<String> participantEmails = entity.getParticipantEmails();
        for (int i = 0; i < participantEmails.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(participantEmails.get(i));
        }
        sb.append("\n");

        List<String> messageHistory = entity.getMessageHistory();
        for (String message : messageHistory) {
            sb.append(message).append("\n");
        }

        return sb.toString();
    }

    @Override
    protected ChatRoom decodeFromString(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Invalid chat room format: content is empty");
        }

        String[] lines = content.split("\n");

        if (lines.length < 1) {
            throw new RuntimeException("Invalid chat room format: insufficient data");
        }

        UUID chatRoomId = UUID.fromString(lines[0].trim());

        List<String> participantEmails = new ArrayList<>();
        if (lines.length >= 2 && !lines[1].trim().isEmpty()) {
            String[] participantEmailStrings = lines[1].split(",");
            for (String emailString : participantEmailStrings) {
                participantEmails.add(emailString.trim());
            }
        }

        List<String> messageHistory = new ArrayList<>();
        for (int i = 2; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                messageHistory.add(lines[i]);
            }
        }

        return new ChatRoom(chatRoomId, participantEmails, messageHistory);
    }

    @Override
    public Optional<ChatRoom> findById(UUID chatRoomId) {
        String chatRoomIdStr = chatRoomId.toString();
        Object lock = getLock(chatRoomIdStr);
        synchronized (lock) {
            String fileName = chatRoomIdStr;
            if (!fileExists(fileName)) {
                return Optional.empty();
            }
            try {
                ChatRoom chatRoom = readFromFile(fileName);
                return Optional.of(chatRoom);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }
    }

    @Override
    public List<ChatRoom> findAll() {
        return readAllFromDirectory();
    }

    @Override
    public void save(ChatRoom chatRoom) {
        String chatRoomId = chatRoom.getChatRoomId().toString();
        Object lock = getLock(chatRoomId);
        synchronized (lock) {
            String fileName = chatRoom.getChatRoomId().toString();
            writeToFile(chatRoom, fileName);
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
