package server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRoom {
    private final UUID chatRoomId;
    private List<String> participantEmails;
    private List<String> messageHistory;

    public ChatRoom(ArrayList<String> participantEmails) {
        this.chatRoomId = UUID.randomUUID();
        this.participantEmails = participantEmails;
        this.messageHistory = new ArrayList<String>();
    }

    public ChatRoom(UUID chatRoomId, List<String> participantEmails, List<String> messageHistory) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId must not be null");
        }
        if (participantEmails == null) {
            throw new IllegalArgumentException("participantEmails must not be null");
        }
        if (messageHistory == null) {
            throw new IllegalArgumentException("messageHistory must not be null");
        }
        this.chatRoomId = chatRoomId;
        this.participantEmails = new ArrayList<>(participantEmails);
        this.messageHistory = new ArrayList<>(messageHistory);
    }

    public UUID getChatRoomId() {
        return chatRoomId;
    }

    public List<String> getParticipantEmails() {
        return participantEmails;
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }

    public void addParticipant(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        if (!participantEmails.contains(email)) {
            participantEmails.add(email);
        }
    }

    public void removeParticipant(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        participantEmails.remove(email);
    }

    public void addMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        messageHistory.add(message);
    }
}
