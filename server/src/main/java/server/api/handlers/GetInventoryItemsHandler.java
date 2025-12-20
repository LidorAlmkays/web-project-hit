package server.api.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;

public class GetInventoryItemsHandler implements SocketHandler {
    private final BranchItemService branchItemService;
    private final Gson gson = new Gson();

    public GetInventoryItemsHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String requestJson = gson.toJson(data);
        JsonObject request = gson.fromJson(requestJson, JsonObject.class);
        UUID branchId = UUID.fromString(request.get("branchId").getAsString());

        List<BranchInventoryItem> items = branchItemService.getBranchItems(branchId);

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeUTF(gson.toJson(items));
    }
}