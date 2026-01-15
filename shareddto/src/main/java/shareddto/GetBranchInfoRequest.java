package shareddto;

public class GetBranchInfoRequest {
    private String branchId;

    public GetBranchInfoRequest(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchId() {
        return branchId;
    }
}