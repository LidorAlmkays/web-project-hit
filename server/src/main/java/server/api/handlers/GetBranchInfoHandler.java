package server.api.handlers;

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
            Branch branch = branchOptional.orElseThrow(() -> new IllegalArgumentException("Branch not found"));

            sendSuccess(clientSocket, branch);
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.GET_BRANCH_INFO, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.GET_BRANCH_INFO, message));
    }
}