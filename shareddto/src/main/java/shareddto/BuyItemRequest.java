package shareddto;

public class BuyItemRequest {
    private String branchId;
    private String itemId;
    private String customerId;
    private int quantity;

    public BuyItemRequest(String branchId, String itemId, String customerId, int quantity) {
        this.branchId = branchId;
        this.itemId = itemId;
        this.customerId = customerId;
        this.quantity = quantity;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getQuantity() {
        return quantity;
    }
}