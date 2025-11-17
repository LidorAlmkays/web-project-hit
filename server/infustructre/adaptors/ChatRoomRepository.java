package server.infustructre.adaptors;

import server.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository {
    Optional<ChatRoom> findById(UUID chatRoomId);

    List<ChatRoom> findAll();

    void save(ChatRoom chatRoom);

    void deleteById(UUID chatRoomId);

    boolean existsById(UUID chatRoomId);
}