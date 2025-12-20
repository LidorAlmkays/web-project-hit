package server.api.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

import server.application.adaptors.BranchService;
import server.domain.Branch;

public class GetBranchInfoHandler implements SocketHandler {
    private final BranchService branchService;
    private final Gson gson = new Gson();

    public GetBranchInfoHandler(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String requestJson = gson.toJson(data);
        JsonObject request = gson.fromJson(requestJson, JsonObject.class);
        UUID branchId = UUID.fromString(request.get("branchId").getAsString());

        Optional<Branch> branchOptional = branchService.getBranch(branchId);
        Branch branch = branchOptional.orElseThrow(() -> new Exception("Branch not found"));

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeUTF(gson.toJson(branch));
    }
}