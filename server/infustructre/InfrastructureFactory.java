package server.infustructre;

import server.infustructre.adaptors.ChatRoomRepository;
import server.infustructre.adaptors.InventoryItemRepository;
import server.infustructre.adaptors.UserAccountRepository;
import server.infustructre.persistentTxtStorage.FileChatRoomRepository;
import server.infustructre.persistentTxtStorage.FileInventoryItemRepository;
import server.infustructre.persistentTxtStorage.FileUserAccountRepository;

public class InfrastructureFactory {

    public UserAccountRepository createUserAccountRepository() {
        return new FileUserAccountRepository();
    }

    public InventoryItemRepository createInventoryItemRepository() {
        return new FileInventoryItemRepository();
    }

    public ChatRoomRepository createChatRoomRepository() {
        return new FileChatRoomRepository();
    }
}
