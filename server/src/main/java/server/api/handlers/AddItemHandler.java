package server.api.handlers;

import shareddto.AddItemRequest;
import shareddto.EventType;
import shareddto.SocketMessage;

import java.net.Socket;
import java.util.UUID;

import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;

public class AddItemHandler extends AbstractSocketHandler {
    private final BranchItemService branchItemService;

    public AddItemHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String requestJson = gson.toJson(data);
        AddItemRequest request = gson.fromJson(requestJson, AddItemRequest.class);

        try {
            BranchInventoryItem newItem = branchItemService.addItem(
                    UUID.fromString(request.getBranchId()),
                    request.getProductName(),
                    request.getCategory(),
                    request.getUnitPrice(),
                    request.getQuantity()
            );

            sendSuccess(clientSocket, newItem);
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in AddItemHandler:");
            e.printStackTrace();
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.ADD_INVENTORY_ITEM, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.ADD_INVENTORY_ITEM, message));
    }
}