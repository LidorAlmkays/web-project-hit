package server.application.adaptors;

import java.util.UUID;

public interface ChatRoomService {

    UUID createChatRoom();

    void joinChatRoom(UUID chatRoomId, String userEmail);

    int getParticipantCount(UUID chatRoomId);

    void disconnectUser(String userEmail);

    void addMessage(UUID chatRoomId, String userEmail, String message);
}
