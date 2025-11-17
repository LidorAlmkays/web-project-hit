package server.application;

import server.application.adaptors.ChatRoomService;
import server.application.adaptors.InventoryItemService;
import server.application.adaptors.UserAccountService;
import server.application.services.ChatRoomServiceImpl;
import server.application.services.InventoryItemServiceImpl;
import server.application.services.UserAccountServiceImpl;
import server.infustructre.adaptors.ChatRoomRepository;
import server.infustructre.adaptors.InventoryItemRepository;
import server.infustructre.adaptors.UserAccountRepository;

public class ApplicationFactory {
    public InventoryItemService createInventoryItemService(InventoryItemRepository repository) {
        return new InventoryItemServiceImpl(repository);
    }

    public UserAccountService createUserAccountService(UserAccountRepository repository) {
        return new UserAccountServiceImpl(repository);
    }

    public ChatRoomService createChatRoomService(ChatRoomRepository chatRoomRepository,
            UserAccountService userAccountService) {
        return new ChatRoomServiceImpl(chatRoomRepository, userAccountService);
    }
}
