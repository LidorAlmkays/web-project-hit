package server.application.adaptors;

import server.domain.InventoryItem;
import java.util.List;
import java.util.Optional;

public interface InventoryItemService {
    InventoryItem purchaseItem(String itemName, int quantity);

    List<InventoryItem> getAllItems();

    Optional<InventoryItem> getItemByName(String itemName);

    InventoryItem restockItem(String itemName, int quantity);

    InventoryItem createItem(String itemName, double unitPrice, int initialQuantity);

    InventoryItem updateItemPrice(String itemName, double newPrice);

    boolean checkAvailability(String itemName, int quantity);

    int getStockQuantity(String itemName);

    int getTotalPurchasedCount(String itemName);

    boolean deleteItem(String itemName);

    boolean itemExists(String itemName);
}
