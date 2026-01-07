package server.api.handlers;

import com.google.gson.JsonObject;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

import server.application.adaptors.BranchService;
import server.domain.Branch;
import shareddto.EventType;
import shareddto.GetBranchInfoRequest;
import shareddto.SocketMessage;

public class GetBranchInfoHandler extends AbstractSocketHandler {
    private final BranchService branchService;

    public GetBranchInfoHandler(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            String requestJson = gson.toJson(data);
            GetBranchInfoRequest request = gson.fromJson(requestJson, GetBranchInfoRequest.class);
            UUID branchId = UUID.fromString(request.getBranchId());

            Optional<Branch> branchOptional = branchService.getBranch(branchId);
            Branch branch = branchOptional.orElseThrow(() -> new Exception("Branch not found"));

            sendMessage(clientSocket, new SocketMessage(EventType.GET_BRANCH_INFO, branch));
        } catch (Exception e) {
            JsonObject errorData = new JsonObject();
            errorData.addProperty("error", e.getMessage());
            sendMessage(clientSocket, new SocketMessage(EventType.GET_BRANCH_INFO, errorData));
        }
    }
}