package shareddto.chat;

import java.util.UUID;

public class ChatPacket {
    private UUID senderId;
    private UUID receiverId;
    private String message;

    public ChatPacket() {
    }

    public ChatPacket(UUID senderId, UUID receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
