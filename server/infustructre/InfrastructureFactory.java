package server.infustructre;

import server.infustructre.adaptors.ChatRoomMessageRepository;
import server.infustructre.adaptors.InventoryItemRepository;
import server.infustructre.adaptors.SocketMessageSender;
import server.infustructre.adaptors.UserAccountRepository;
import server.infustructre.persistentTxtStorage.FileChatRoomMessageRepository;
import server.infustructre.persistentTxtStorage.FileInventoryItemRepository;
import server.infustructre.persistentTxtStorage.FileUserAccountRepository;
import server.infustructre.socket.SocketMessageSenderImpl;

public class InfrastructureFactory {

    public UserAccountRepository createUserAccountRepository() {
        return new FileUserAccountRepository();
    }

    public InventoryItemRepository createInventoryItemRepository() {
        return new FileInventoryItemRepository();
    }

    public ChatRoomMessageRepository createChatRoomMessageRepository() {
        return new FileChatRoomMessageRepository();
    }

    public SocketMessageSender createSocketMessageSender() {
        return new SocketMessageSenderImpl();
    }
}
