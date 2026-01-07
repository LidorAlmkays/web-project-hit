package server.api.handlers;

import com.google.gson.JsonObject;
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

            sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_INVENTORY_ITEM, updatedItem));
        } catch (Exception e) {
            JsonObject errorData = new JsonObject();
            errorData.addProperty("error", e.getMessage());
            sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_INVENTORY_ITEM, errorData));
        }
    }
}