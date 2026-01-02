package server.api.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

import server.application.adaptors.BranchService;
import server.domain.Branch;
import shareddto.GetBranchInfoRequest;

public class GetBranchInfoHandler implements SocketHandler {
    private final BranchService branchService;
    private final Gson gson = new Gson();

    public GetBranchInfoHandler(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String requestJson = gson.toJson(data);
        GetBranchInfoRequest request = gson.fromJson(requestJson, GetBranchInfoRequest.class);
        UUID branchId = UUID.fromString(request.getBranchId());

        Optional<Branch> branchOptional = branchService.getBranch(branchId);
        Branch branch = branchOptional.orElseThrow(() -> new Exception("Branch not found"));

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        
        JsonObject response = new JsonObject();
        response.add("data", gson.toJsonTree(branch));
        out.writeUTF(gson.toJson(response));
    }
}