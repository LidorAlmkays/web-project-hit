package server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRoomMessageStore {
    private final UUID chatRoomId;
    private List<String> messageHistory;

    public ChatRoomMessageStore() {
        this.chatRoomId = UUID.randomUUID();
        this.messageHistory = new ArrayList<String>();
    }

    public ChatRoomMessageStore(UUID chatRoomId, List<String> messageHistory) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId must not be null");
        }
        if (messageHistory == null) {
            throw new IllegalArgumentException("messageHistory must not be null");
        }
        this.chatRoomId = chatRoomId;
        this.messageHistory = new ArrayList<>(messageHistory);
    }

    public UUID getChatRoomId() {
        return chatRoomId;
    }

    public List<String> getMessageHistory() {
        return new ArrayList<>(messageHistory); // Copy of the message history to not allow refrance change by mistake
    }

    public void addMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        messageHistory.add(message);
    }
}
