package frontend.dto.employeemanagement.request;

public class BranchEmployeesRequest {
    private String branchId;

    public BranchEmployeesRequest() {
    }

    public BranchEmployeesRequest(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}
