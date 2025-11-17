package server.infustructre.adaptors;

import server.domain.InventoryItem;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository {
    Optional<InventoryItem> findByName(String itemName);

    List<InventoryItem> findAll();

    void save(InventoryItem item);

    void updateItemByName(String itemName, InventoryItem item);

    void deleteByName(String itemName);

    boolean existsByName(String itemName);

    boolean checkAvailability(String itemName, int quantity);
}
