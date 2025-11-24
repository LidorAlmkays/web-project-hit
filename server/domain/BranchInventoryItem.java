package server.domain;

import java.util.UUID;

public class BranchInventoryItem {
    private final UUID itemId;// index
    private final UUID branchId;// index
    private String productName;
    private String category;
    private double unitPrice;
    private int quantityInStock;
    private int totalBought;
    private int totalSold;
    private double totalRevenue;

    public BranchInventoryItem(UUID branchId, String productName, String category, double unitPrice) {
        if (branchId == null) {
            throw new IllegalArgumentException("branchId must not be null");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("productName must not be null or empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
        this.itemId = UUID.randomUUID();
        this.branchId = branchId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantityInStock = 0;
        this.totalBought = 0;
        this.totalSold = 0;
        this.totalRevenue = 0.0;
    }

    public BranchInventoryItem(UUID itemId, UUID branchId, String productName, String category, double unitPrice,
            int quantityInStock, int totalBought, int totalSold, double totalRevenue) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId must not be null");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("branchId must not be null");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("productName must not be null or empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
        if (quantityInStock < 0) {
            throw new IllegalArgumentException("quantityInStock must be non-negative");
        }
        if (totalBought < 0) {
            throw new IllegalArgumentException("totalBought must be non-negative");
        }
        if (totalSold < 0) {
            throw new IllegalArgumentException("totalSold must be non-negative");
        }
        if (totalRevenue < 0) {
            throw new IllegalArgumentException("totalRevenue must be non-negative");
        }
        this.itemId = itemId;
        this.branchId = branchId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantityInStock = quantityInStock;
        this.totalBought = totalBought;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
    }

    public UUID getItemId() {
        return itemId;
    }

    public UUID getBranchId() {
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

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public int getTotalBought() {
        return totalBought;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("productName must not be null or empty");
        }
        this.productName = productName;
    }

    public void setCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        this.category = category;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
        this.unitPrice = unitPrice;
    }

    public void restock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Restock quantity must be non-negative");
        }
        this.quantityInStock += quantity;
        this.totalBought += quantity;
    }

    public void sell(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Sell quantity must be non-negative");
        }
        if (quantity > quantityInStock) {
            throw new IllegalArgumentException("Cannot sell more than available in stock");
        }
        this.quantityInStock -= quantity;
        this.totalSold += quantity;
        this.totalRevenue += (quantity * unitPrice);
    }

    public BranchInventoryItem createCopy() {
        return new BranchInventoryItem(
                this.itemId,
                this.branchId,
                this.productName,
                this.category,
                this.unitPrice,
                this.quantityInStock,
                this.totalBought,
                this.totalSold,
                this.totalRevenue);
    }

    @Override
    public String toString() {
        return "BranchInventoryItem{" +
                "itemId=" + itemId +
                ", branchId=" + branchId +
                ", productName='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantityInStock=" + quantityInStock +
                ", totalBought=" + totalBought +
                ", totalSold=" + totalSold +
                ", totalRevenue=" + totalRevenue +
                '}';
    }
}
