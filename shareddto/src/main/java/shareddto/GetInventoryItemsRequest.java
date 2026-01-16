package shareddto;

public class GetInventoryItemsRequest {
    private String branchId;

    public GetInventoryItemsRequest(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchId() {
        return branchId;
    }
}