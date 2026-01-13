package server.api.handlers;

import java.net.Socket;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;
import shareddto.BuyItemRequest;
import shareddto.EventType;
import shareddto.SocketMessage;

public class BuyItemHandler extends AbstractSocketHandler {
    private final BranchItemService branchItemService;

    public BuyItemHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            String requestJson = gson.toJson(data);
            BuyItemRequest request = gson.fromJson(requestJson, BuyItemRequest.class);

            UUID branchId;
            try {
                branchId = UUID.fromString(request.getBranchId());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Branch ID format: " + request.getBranchId());
            }

            UUID customerId;
            try {
                customerId = UUID.fromString(request.getCustomerId());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Validation Error: '" + request.getCustomerId() + "' is not a valid Customer UUID format.");
            }

            UUID itemId;
            try {
                itemId = UUID.fromString(request.getItemId());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Validation Error: '" + request.getItemId() + "' is not a valid Item UUID format.");
            }

            int quantity = request.getQuantity();

            BranchInventoryItem updatedItem = branchItemService.buyItem(branchId, itemId, customerId, quantity);
           
            sendSuccess(clientSocket, updatedItem);
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in BuyItemHandler:");
            e.printStackTrace();
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.BUY_INVENTORY_ITEM, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.BUY_INVENTORY_ITEM, message));
    }
}