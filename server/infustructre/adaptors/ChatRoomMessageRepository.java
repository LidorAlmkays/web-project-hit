package server.infustructre.adaptors;

import server.domain.ChatRoomMessageStore;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomMessageRepository {
    Optional<ChatRoomMessageStore> findById(UUID chatRoomId);

    List<ChatRoomMessageStore> findAll();

    void save(ChatRoomMessageStore messageStore);

    void deleteById(UUID chatRoomId);

    boolean existsById(UUID chatRoomId);
}