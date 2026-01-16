package server.api.handlers;

import java.net.Socket;
import java.util.List;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;
import shareddto.EventType;
import shareddto.GetInventoryItemsRequest;
import shareddto.SocketMessage;

public class GetInventoryItemsHandler extends AbstractSocketHandler {
    private final BranchItemService branchItemService;

    public GetInventoryItemsHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            String requestJson = gson.toJson(data);
            GetInventoryItemsRequest request = gson.fromJson(requestJson, GetInventoryItemsRequest.class);
            UUID branchId = UUID.fromString(request.getBranchId());

            List<BranchInventoryItem> items = branchItemService.getBranchItems(branchId);

            sendSuccess(clientSocket, items);
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.GET_INVERTORY_ITEMS, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.GET_INVERTORY_ITEMS, message));
    }
}