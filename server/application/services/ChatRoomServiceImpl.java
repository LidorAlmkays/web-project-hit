package server.application.services;

import server.application.adaptors.ChatRoomService;
import server.application.adaptors.SocketManager;
import server.application.adaptors.UserAccountService;
import server.domain.ChatRoomMessageStore;
import server.infustructre.adaptors.ChatRoomMessageRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomMessageRepository repository;
    private final UserAccountService userAccountService;
    private final SocketManager socketManager;

    private static final Map<UUID, Set<String>> roomToParticipants = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, UUID> emailToRoom = Collections.synchronizedMap(new HashMap<>());
    private static final Map<UUID, Object> chatRoomIdMessageLockMap = Collections.synchronizedMap(new HashMap<>());
    private static final Object creationMutex = new Object();

    public ChatRoomServiceImpl(ChatRoomMessageRepository repository, UserAccountService userAccountService,
            SocketManager socketManager) {
        if (repository == null) {
            throw new IllegalArgumentException("ChatRoomMessageRepository must not be null");
        }
        if (userAccountService == null) {
            throw new IllegalArgumentException("UserAccountService must not be null");
        }
        if (socketManager == null) {
            throw new IllegalArgumentException("SocketManager must not be null");
        }
        this.repository = repository;
        this.userAccountService = userAccountService;
        this.socketManager = socketManager;
    }

    @Override
    public UUID createChatRoom() {
        ChatRoomMessageStore messageStore = new ChatRoomMessageStore();
        repository.save(messageStore);
        roomToParticipants.put(messageStore.getChatRoomId(), Collections.synchronizedSet(new java.util.HashSet<>()));
        System.out.println("Chat room created: " + messageStore.getChatRoomId());
        return messageStore.getChatRoomId();
    }

    @Override
    public void joinChatRoom(UUID chatRoomId, String userEmail) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be null or empty");
        }
        if (!repository.existsById(chatRoomId)) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }

        if (!userAccountService.getUsernameByEmail(userEmail).isPresent()) {
            throw new IllegalArgumentException("User not found: " + userEmail);
        }

        UUID currentRoom = emailToRoom.get(userEmail);
        if (currentRoom != null) {
            if (currentRoom.equals(chatRoomId)) {// trying to join the same room as he is currently in
                return;
            } else {
                throw new IllegalArgumentException(
                        "User " + userEmail + " is already in room " + currentRoom + ". Cannot join another room.");
            }
        }

        Set<String> participants = roomToParticipants.get(chatRoomId);
        if (participants == null) {
            participants = Collections.synchronizedSet(new java.util.HashSet<>());
            roomToParticipants.put(chatRoomId, participants);
        }
        participants.add(userEmail);
        emailToRoom.put(userEmail, chatRoomId);

        Optional<ChatRoomMessageStore> messageStoreOpt = repository.findById(chatRoomId);
        if (messageStoreOpt.isPresent()) {
            ChatRoomMessageStore messageStore = messageStoreOpt.get();
            List<String> messageHistory = messageStore.getMessageHistory();
            if (!messageHistory.isEmpty()) {
                socketManager.sendMessagesToEmail(userEmail, messageHistory);
            }
        }

        String username = userAccountService.getUsernameByEmail(userEmail).get();
        String joinMessage = userEmail + " has joined the chat under the name " + username;
        sendMessageToAllParticipants(chatRoomId, joinMessage);
    }

    @Override
    public int getParticipantCount(UUID chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }

        if (!repository.existsById(chatRoomId)) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }

        Set<String> participants = roomToParticipants.get(chatRoomId);
        if (participants != null) {
            return participants.size();
        } else {
            return 0;
        }
    }

    @Override
    public void disconnectUser(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be null or empty");
        }

        UUID chatRoomId = emailToRoom.get(userEmail);
        if (chatRoomId == null) {
            throw new IllegalArgumentException("User " + userEmail + " is not in any room");
        }

        if (!repository.existsById(chatRoomId)) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }

        Set<String> participants = roomToParticipants.get(chatRoomId);
        if (participants == null || !participants.contains(userEmail)) {
            throw new IllegalArgumentException("User " + userEmail + " is not in room " + chatRoomId);
        }

        participants.remove(userEmail);
        if (participants.isEmpty()) {
            roomToParticipants.remove(chatRoomId);
        }

        emailToRoom.remove(userEmail);

        String username = userAccountService.getUsernameByEmail(userEmail).get();
        String disconnectMessage = userEmail + " has left the chat under the name " + username;
        sendMessageToAllParticipants(chatRoomId, disconnectMessage);
    }

    @Override
    public void addMessage(UUID chatRoomId, String userEmail, String message) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message must not be null or empty");
        }
        UUID userRoom = emailToRoom.get(userEmail);
        if (userRoom == null || !userRoom.equals(chatRoomId)) {
            throw new IllegalArgumentException("User " + userEmail + " is not in room " + chatRoomId);
        }
        Optional<String> usernameOpt = userAccountService.getUsernameByEmail(userEmail);
        if (usernameOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userEmail);
        }
        String username = usernameOpt.get();

        Optional<ChatRoomMessageStore> messageStoreOpt = repository.findById(chatRoomId);
        if (messageStoreOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }

        ChatRoomMessageStore messageStore = messageStoreOpt.get();
        String formattedMessage = username + ": " + message;
        messageStore.addMessage(formattedMessage);
        repository.save(messageStore);

        sendMessageToAllParticipants(chatRoomId, formattedMessage);
    }

    private Object getLock(UUID chatRoomId) {
        Object lock = chatRoomIdMessageLockMap.get(chatRoomId);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = chatRoomIdMessageLockMap.get(chatRoomId);
                if (lock == null) {
                    lock = new Object();
                    chatRoomIdMessageLockMap.put(chatRoomId, lock);
                }
            }
        }
        return lock;
    }

    private void sendMessageToAllParticipants(UUID chatRoomId, String message) {
        Object lock = getLock(chatRoomId);
        synchronized (lock) {
            Set<String> participants = roomToParticipants.get(chatRoomId);
            if (participants != null && !participants.isEmpty()) {
                for (String participantEmail : participants) {
                    socketManager.sendMessageToEmail(participantEmail, message);
                }
            }
        }
    }
}
