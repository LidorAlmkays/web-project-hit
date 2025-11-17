package server.infustructre.persistentTxtStorage;

import server.domain.InventoryItem;
import server.infustructre.adaptors.InventoryItemRepository;
import server.config.Config;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileInventoryItemRepository extends AbstractFileRepository<InventoryItem>
        implements InventoryItemRepository {
    private final Map<String, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();

    public FileInventoryItemRepository() {
        super(Config.getInventoryItemsDir());
    }

    private Object getLock(String itemName) {
        String sanitized = sanitizeFileName(itemName);
        Object lock = locks.get(sanitized);
        if (lock == null) {// this is for when 2 threads try to get the lock at the same TIME for an item
                           // that still dosnt exists yet (for safty :3)
            synchronized (creationMutex) {
                lock = locks.get(sanitized);
                if (lock == null) {
                    lock = new Object();
                    locks.put(sanitized, lock);
                }
            }
        }
        return lock;
    }

    private String sanitizeFileName(String itemName) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < itemName.length(); i++) {
            char c = itemName.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') || c == '.' || c == '-') {
                result.append(c);
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    @Override
    protected String encode(InventoryItem entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getItemName()).append("\n");
        sb.append(entity.getUnitPrice()).append("\n");
        sb.append(entity.getQuantityInStock()).append("\n");
        sb.append(entity.getTotalPurchasedCount()).append("\n");
        return sb.toString();
    }

    @Override
    protected InventoryItem decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 4) {
            throw new RuntimeException("Invalid inventory item format: insufficient data");
        }

        String itemName = lines[0].trim();
        double unitPrice = Double.parseDouble(lines[1].trim());
        int quantityInStock = Integer.parseInt(lines[2].trim());
        int totalPurchasedCount = Integer.parseInt(lines[3].trim());

        return new InventoryItem(itemName, unitPrice, quantityInStock, totalPurchasedCount);
    }

    @Override
    public Optional<InventoryItem> findByName(String itemName) {
        Object lock = getLock(itemName);
        synchronized (lock) {
            String fileName = sanitizeFileName(itemName);
            if (!fileExists(fileName)) {
                return Optional.empty();
            }
            try {
                InventoryItem item = readFromFile(fileName);
                return Optional.of(item);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }
    }

    @Override
    public List<InventoryItem> findAll() {
        return readAllFromDirectory();
    }

    @Override
    public void save(InventoryItem item) {
        String itemName = item.getItemName();
        Object lock = getLock(itemName);
        synchronized (lock) {
            String fileName = sanitizeFileName(itemName);
            if (existsByName(itemName)) {
                throw new IllegalArgumentException("Item name already exists: " + itemName);
            }
            writeToFile(item, fileName);
        }
    }

    @Override
    public void updateItemByName(String itemName, InventoryItem item) {
        Object lock = getLock(itemName);
        synchronized (lock) {
            if (!existsByName(itemName)) {
                throw new IllegalArgumentException("Item does not exist: " + itemName);
            }
            String newName = item.getItemName();
            if (!itemName.equals(newName)) {
                if (existsByName(newName)) {
                    throw new IllegalArgumentException("Item name already exists: " + newName);
                }
                String oldFileName = sanitizeFileName(itemName);
                deleteFile(oldFileName);
            }
            String fileName = sanitizeFileName(newName);
            writeToFile(item, fileName);
        }
    }

    @Override
    public void deleteByName(String itemName) {
        String fileName = sanitizeFileName(itemName);
        deleteFile(fileName);
    }

    @Override
    public boolean existsByName(String itemName) {
        Object lock = getLock(itemName);
        synchronized (lock) {
            String fileName = sanitizeFileName(itemName);
            return fileExists(fileName);
        }
    }

    @Override
    public boolean checkAvailability(String itemName, int quantity) {
        Optional<InventoryItem> itemOpt = findByName(itemName);
        if (itemOpt.isEmpty()) {
            return false;
        }
        InventoryItem item = itemOpt.get();
        return item.getQuantityInStock() >= quantity && quantity >= 0;
    }
}
