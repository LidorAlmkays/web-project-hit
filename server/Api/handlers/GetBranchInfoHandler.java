package server.api.handlers;

import java.net.Socket;
import java.util.List;

import server.api.dto.GetBranchInfo;
import server.application.adaptors.BranchService;
import server.domain.BranchInventoryItem;

public class GetBranchInfoHandler implements SocketHandler {
    private final BranchService branchService;

    public GetBranchInfoHandler(BranchService branchService) {
        this.branchService = branchService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        List<BranchInventoryItem> branchInfo = (GetBranchInfo) data;
    }
}