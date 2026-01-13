package server.api.handlers;

import java.net.Socket;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.UpdateStockRequest;

public class UpdateInventoryItemHandler extends AbstractSocketHandler {
    private final BranchItemService branchItemService;

    public UpdateInventoryItemHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            String requestJson = gson.toJson(data);
            UpdateStockRequest request = gson.fromJson(requestJson, UpdateStockRequest.class);

            UUID branchId = UUID.fromString(request.getBranchId());
            UUID itemId = UUID.fromString(request.getItemId());
            int quantity = request.getQuantity();

            BranchInventoryItem updatedItem = branchItemService.restockItem(branchId, itemId, quantity);

            sendSuccess(clientSocket, updatedItem);
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_INVENTORY_ITEM, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_INVENTORY_ITEM, message));
    }
}