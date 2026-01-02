package shareddto;

public class UpdateStockRequest {
    private String branchId;
    private String itemId;
    private int quantity;

    public UpdateStockRequest(String branchId, String itemId, int quantity) {
        this.branchId = branchId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
}