package server.api.dto;

import java.util.UUID;

public class GetBranchInfo {
    public GetBranchInfo(UUID branchId) {
        this.branchId = branchId;
    }

    private UUID branchId;
}
