package server.application.services;

import server.application.adaptors.InventoryItemService;
import server.domain.InventoryItem;
import server.infustructre.adaptors.InventoryItemRepository;
import java.util.List;
import java.util.Optional;

public class InventoryItemServiceImpl implements InventoryItemService {
    private final InventoryItemRepository repository;

    public InventoryItemServiceImpl(InventoryItemRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("InventoryItemRepository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public InventoryItem purchaseItem(String itemName, int quantity) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must not be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        Optional<InventoryItem> itemOpt = repository.findByName(itemName);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found: " + itemName);
        }
        InventoryItem item = itemOpt.get();
        if (quantity > item.getQuantityInStock()) {
            throw new IllegalArgumentException(
                    "Cannot purchase " + quantity + " items. Only " + item.getQuantityInStock()
                            + " available in stock.");
        }
        item.purchaseItem(quantity);
        repository.updateItemByName(itemName, item);
        return item;
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return repository.findAll();
    }

    @Override
    public InventoryItem restockItem(String itemName, int quantity) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must not be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        Optional<InventoryItem> itemOpt = repository.findByName(itemName);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found: " + itemName);
        }
        InventoryItem item = itemOpt.get();
        item.restock(quantity);
        repository.updateItemByName(itemName, item);
        return item;
    }

    @Override
    public InventoryItem createItem(String itemName, double unitPrice, int initialQuantity) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must not be null");
        }
        if (itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name must not be empty");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity must be non-negative");
        }

        if (repository.existsByName(itemName)) {
            throw new IllegalArgumentException("Item name already exists: " + itemName);
        }

        InventoryItem item = new InventoryItem(itemName, unitPrice, initialQuantity, 0);
        repository.save(item);
        return item;
    }

    @Override
    public InventoryItem updateItemPrice(String itemName, double newPrice) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must not be null");
        }
        if (newPrice < 0) {
            throw new IllegalArgumentException("New price must be non-negative");
        }

        Optional<InventoryItem> itemOpt = repository.findByName(itemName);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found: " + itemName);
        }
        InventoryItem item = itemOpt.get();
        item.updatePrice(newPrice);
        repository.updateItemByName(itemName, item);
        return item;
    }

    @Override
    public boolean deleteItem(String itemName) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must not be null");
        }

        if (!repository.existsByName(itemName)) {
            return false;
        }

        repository.deleteByName(itemName);
        return true;
    }

}
