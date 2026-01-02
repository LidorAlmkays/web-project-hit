package server.api.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import server.application.adaptors.BranchItemService;
import server.domain.BranchInventoryItem;
import shareddto.GetInventoryItemsRequest;

public class GetInventoryItemsHandler implements SocketHandler {
    private final BranchItemService branchItemService;
    private final Gson gson = new Gson();

    public GetInventoryItemsHandler(BranchItemService branchItemService) {
        this.branchItemService = branchItemService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String requestJson = gson.toJson(data);
        GetInventoryItemsRequest request = gson.fromJson(requestJson, GetInventoryItemsRequest.class);
        UUID branchId = UUID.fromString(request.getBranchId());

        List<BranchInventoryItem> items = branchItemService.getBranchItems(branchId);

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        JsonObject response = new JsonObject();
        response.add("data", gson.toJsonTree(items));
        out.writeUTF(gson.toJson(response));
    }
}