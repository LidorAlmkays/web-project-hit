package server.application.adaptors;

import java.util.List;
import java.util.UUID;

public interface ChatRoomService {
    UUID createChatRoom(List<String> participantEmails);

    void joinChatRoom(UUID chatRoomId, String userEmail);

    int getParticipantCount(UUID chatRoomId);

    void disconnectUser(UUID chatRoomId, String userEmail);

    List<String> getAllMessages(UUID chatRoomId);

    void addMessage(UUID chatRoomId, String userEmail, String message);
}
