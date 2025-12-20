package server.api.handlers;

import java.net.Socket;
import server.application.adaptors.BranchService;

public class GetInventoryItemsHandler implements SocketHandler {
    private final BranchService branchService;

    public GetInventoryItemsHandler(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {

    }
}