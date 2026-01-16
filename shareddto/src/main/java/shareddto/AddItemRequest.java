package shareddto;

public class AddItemRequest {
    private String branchId;
    private String productName;
    private String category;
    private double unitPrice;
    private int quantity;

    public AddItemRequest(String branchId, String productName, String category, double unitPrice, int quantity) {
        this.branchId = branchId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}