package server.api.handlers;

import com.google.gson.JsonObject;
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
            
            sendMessage(clientSocket, new SocketMessage(EventType.GET_INVERTORY_ITEMS, items));
        } catch (Exception e) {
            JsonObject errorData = new JsonObject();
            errorData.addProperty("error", e.getMessage());
            sendMessage(clientSocket, new SocketMessage(EventType.GET_INVERTORY_ITEMS, errorData));
        }
    }
}