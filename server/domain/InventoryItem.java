package server.domain;

public class InventoryItem {
    private final String itemName;
    private double unitPrice;
    private int quantityInStock;
    private int totalPurchasedCount;

    public InventoryItem(String itemName, double unitPrice, int quantityInStock, int totalPurchasedCount) {
        if (itemName == null) {
            throw new IllegalArgumentException("itemName must not be null");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
        if (quantityInStock < 0) {
            throw new IllegalArgumentException("quantityInStock must be non-negative");
        }
        if (totalPurchasedCount < 0) {
            throw new IllegalArgumentException("totalPurchasedCount must be non-negative");
        }
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantityInStock = quantityInStock;
        this.totalPurchasedCount = totalPurchasedCount;
    }

    public InventoryItem(String itemName, double unitPrice) {
        if (itemName == null) {
            throw new IllegalArgumentException("itemName must not be null");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantityInStock = 0;
        this.totalPurchasedCount = 0;
    }

    public String getItemName() {
        return itemName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public int getTotalPurchasedCount() {
        return totalPurchasedCount;
    }

    public void purchaseItem(int quantity) {
        this.quantityInStock -= quantity;
        this.totalPurchasedCount += quantity;
    }

    public void restock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Restock quantity must be non-negative");
        }
        this.quantityInStock += quantity;
    }

    public void updatePrice(double newPrice) {
        if (newPrice < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        this.unitPrice = newPrice;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemName='" + itemName + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantityInStock=" + quantityInStock +
                ", totalPurchasedCount=" + totalPurchasedCount +
                '}';
    }
}
