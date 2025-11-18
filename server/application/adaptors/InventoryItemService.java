package server.application.adaptors;

import server.domain.InventoryItem;
import java.util.List;

public interface InventoryItemService {
    InventoryItem purchaseItem(String itemName, int quantity);

    List<InventoryItem> getAllItems();

    InventoryItem restockItem(String itemName, int quantity);

    InventoryItem createItem(String itemName, double unitPrice, int initialQuantity);

    InventoryItem updateItemPrice(String itemName, double newPrice);

    boolean deleteItem(String itemName);
}
