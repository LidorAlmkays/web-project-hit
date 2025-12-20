package server.api.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;

public class UpdateInventoryItemHandler implements SocketHandler {
    private final BranchItemService branchItemService;
    private final Gson gson = new Gson();

    public UpdateInventoryItemHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String requestJson = gson.toJson(data);
        JsonObject request = gson.fromJson(requestJson, JsonObject.class);

        UUID branchId = UUID.fromString(request.get("branchId").getAsString());
        BranchInventoryItem item = gson.fromJson(request.get("item"), BranchInventoryItem.class);

        BranchInventoryItem updatedItem = branchItemService.restockItem(branchId, item.getItemId(), item.getQuantityInStock());

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeUTF(gson.toJson(updatedItem));
    }
}