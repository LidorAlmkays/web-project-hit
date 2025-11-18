package server.application;

import server.application.adaptors.ChatRoomService;
import server.application.adaptors.InventoryItemService;
import server.application.adaptors.SocketManager;
import server.application.adaptors.UserAccountService;
import server.application.services.ChatRoomServiceImpl;
import server.application.services.InventoryItemServiceImpl;
import server.application.services.SocketManagerImpl;
import server.application.services.UserAccountServiceImpl;
import server.infustructre.adaptors.ChatRoomMessageRepository;
import server.infustructre.adaptors.InventoryItemRepository;
import server.infustructre.adaptors.SocketMessageSender;
import server.infustructre.adaptors.UserAccountRepository;

public class ApplicationFactory {
    public InventoryItemService createInventoryItemService(InventoryItemRepository repository) {
        return new InventoryItemServiceImpl(repository);
    }

    public SocketManager createSocketManager(SocketMessageSender socketMessageSender) {
        return new SocketManagerImpl(socketMessageSender);
    }

    public UserAccountService createUserAccountService(UserAccountRepository repository) {
        return new UserAccountServiceImpl(repository);
    }

    public ChatRoomService createChatRoomService(ChatRoomMessageRepository chatRoomMessageRepository,
            UserAccountService userAccountService, SocketManager socketManager) {
        return new ChatRoomServiceImpl(chatRoomMessageRepository, userAccountService, socketManager);
    }
}
