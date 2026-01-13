package shareddto.reporting;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class BranchInventoryReportDto implements Serializable {
    private String reportDate;
    private UUID branchId;
    private int totalUniqueItems;
    private List<InventoryItemDto> items; 

    public BranchInventoryReportDto(String reportDate, UUID branchId, int totalUniqueItems, List<InventoryItemDto> items) {
        this.reportDate = reportDate;
        this.branchId = branchId;
        this.totalUniqueItems = totalUniqueItems;
        this.items = items;
    }

    public static class InventoryItemDto implements Serializable {
        private String itemId;
        private String name;
        private String category;
        private int quantity;

        public InventoryItemDto(String itemId, String name, String category, int quantity) {
            this.itemId = itemId;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
        }
        
        public String getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
    }

    public String getReportDate() { return reportDate; }
    public UUID getBranchId() { return branchId; }
    public int getTotalUniqueItems() { return totalUniqueItems; }
    public List<InventoryItemDto> getItems() { return items; }
}