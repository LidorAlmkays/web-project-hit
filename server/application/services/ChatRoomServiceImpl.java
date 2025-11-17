package server.application.services;

import server.application.adaptors.ChatRoomService;
import server.application.adaptors.UserAccountService;
import server.domain.ChatRoom;
import server.infustructre.adaptors.ChatRoomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository repository;
    private final UserAccountService userAccountService;

    public ChatRoomServiceImpl(ChatRoomRepository repository, UserAccountService userAccountService) {
        if (repository == null) {
            throw new IllegalArgumentException("ChatRoomRepository must not be null");
        }
        if (userAccountService == null) {
            throw new IllegalArgumentException("UserAccountService must not be null");
        }
        this.repository = repository;
        this.userAccountService = userAccountService;
    }

    @Override
    public UUID createChatRoom(List<String> participantEmails) {
        if (participantEmails == null) {
            throw new IllegalArgumentException("Participant emails must not be null");
        }

        ChatRoom chatRoom = new ChatRoom(new ArrayList<>(participantEmails));
        repository.save(chatRoom);
        return chatRoom.getChatRoomId();
    }

    @Override
    public void joinChatRoom(UUID chatRoomId, String userEmail) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }
        if (userEmail == null) {
            throw new IllegalArgumentException("User email must not be null");
        }
        if (userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be empty");
        }

        Optional<ChatRoom> chatRoomOpt = repository.findById(chatRoomId);
        if (chatRoomOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }
        ChatRoom chatRoom = chatRoomOpt.get();

        chatRoom.addParticipant(userEmail);
        repository.save(chatRoom);
    }

    @Override
    public int getParticipantCount(UUID chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }

        Optional<ChatRoom> chatRoomOpt = repository.findById(chatRoomId);
        if (chatRoomOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }
        ChatRoom chatRoom = chatRoomOpt.get();
        return chatRoom.getParticipantEmails().size();
    }

    @Override
    public void disconnectUser(UUID chatRoomId, String userEmail) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }
        if (userEmail == null) {
            throw new IllegalArgumentException("User email must not be null");
        }
        if (userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be empty");
        }

        Optional<ChatRoom> chatRoomOpt = repository.findById(chatRoomId);
        if (chatRoomOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }
        ChatRoom chatRoom = chatRoomOpt.get();

        chatRoom.removeParticipant(userEmail);
        repository.save(chatRoom);
    }

    @Override
    public List<String> getAllMessages(UUID chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }

        Optional<ChatRoom> chatRoomOpt = repository.findById(chatRoomId);
        if (chatRoomOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }
        ChatRoom chatRoom = chatRoomOpt.get();
        return new ArrayList<>(chatRoom.getMessageHistory());
    }

    @Override
    public void addMessage(UUID chatRoomId, String userEmail, String message) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room ID must not be null");
        }
        if (userEmail == null) {
            throw new IllegalArgumentException("User email must not be null");
        }
        if (userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be empty");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        if (message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        Optional<String> usernameOpt = userAccountService.getUsernameByEmail(userEmail);
        if (usernameOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userEmail);
        }
        String username = usernameOpt.get();

        Optional<ChatRoom> chatRoomOpt = repository.findById(chatRoomId);
        if (chatRoomOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found: " + chatRoomId);
        }
        ChatRoom chatRoom = chatRoomOpt.get();

        String formattedMessage = username + ": " + message;
        chatRoom.addMessage(formattedMessage);
        repository.save(chatRoom);
    }
}
