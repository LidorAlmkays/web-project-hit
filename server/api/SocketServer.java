package server.api;

import server.application.adaptors.ChatRoomService;
import server.application.adaptors.InventoryItemService;
import server.application.adaptors.UserAccountService;

public class SocketServer {

    private final ChatRoomService chatRoomService;
    private final InventoryItemService inventoryItemService;
    private final UserAccountService userAccountService;

    public SocketServer(
            ChatRoomService chatRoomService,
            InventoryItemService inventoryItemService,
            UserAccountService userAccountService) {
        this.chatRoomService = chatRoomService;
        this.inventoryItemService = inventoryItemService;
        this.userAccountService = userAccountService;
    }

    public void serve() {
    }
}
