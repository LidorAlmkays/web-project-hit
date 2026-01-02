package server.api.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;
import shareddto.BuyItemRequest;

public class BuyItemHandler implements SocketHandler {
    private final BranchItemService branchItemService;
    private final Gson gson = new Gson();

    public BuyItemHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        try {
            String requestJson = gson.toJson(data);
            BuyItemRequest request = gson.fromJson(requestJson, BuyItemRequest.class);

            UUID branchId = UUID.fromString(request.getBranchId());
            UUID itemId = UUID.fromString(request.getItemId());
            UUID customerId = UUID.fromString(request.getCustomerId());
            int quantity = request.getQuantity();

            BranchInventoryItem updatedItem = branchItemService.buyItem(branchId, itemId, customerId, quantity);

            JsonObject response = new JsonObject();
            response.add("data", gson.toJsonTree(updatedItem));
            out.writeUTF(gson.toJson(response));
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("error", e.getMessage());
            out.writeUTF(gson.toJson(response));
        }
    }
}
