package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.BranchInventoryItem;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileBranchInventoryItemRepository extends AbstractFileRepository<BranchInventoryItem>
        implements BranchInventoryItemRepository {
    private final Map<UUID, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();
    private final Map<UUID, BranchInventoryItem> cache = Collections.synchronizedMap(new HashMap<>());
    // branchId -> List of itemIds
    private final Map<UUID, List<UUID>> branchIndex = Collections.synchronizedMap(new HashMap<>());

    public FileBranchInventoryItemRepository() {
        super(Config.getBranchInventoryItemsDir());
        loadCache();
    }

    private Object getLock(UUID itemId) {
        Object lock = locks.get(itemId);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = locks.get(itemId);
                if (lock == null) {
                    lock = new Object();
                    locks.put(itemId, lock);
                }
            }
        }
        return lock;
    }

    private String getFileName(UUID itemId) {
        return itemId.toString();
    }

    @Override
    protected String encode(BranchInventoryItem entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getItemId().toString()).append("\n");
        sb.append(entity.getBranchId().toString()).append("\n");
        sb.append(entity.getProductName()).append("\n");
        sb.append(entity.getCategory()).append("\n");
        sb.append(entity.getUnitPrice()).append("\n");
        sb.append(entity.getQuantityInStock()).append("\n");
        sb.append(entity.getTotalBought()).append("\n");
        sb.append(entity.getTotalSold()).append("\n");
        sb.append(entity.getTotalRevenue()).append("\n");
        return sb.toString();
    }

    @Override
    protected BranchInventoryItem decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 9) {
            throw new RuntimeException("Invalid branch inventory item format: insufficient data");
        }

        UUID itemId = UUID.fromString(lines[0].trim());
        UUID branchId = UUID.fromString(lines[1].trim());
        String productName = lines[2].trim();
        String category = lines[3].trim();
        double unitPrice = Double.parseDouble(lines[4].trim());
        int quantityInStock = Integer.parseInt(lines[5].trim());
        int totalBought = Integer.parseInt(lines[6].trim());
        int totalSold = Integer.parseInt(lines[7].trim());
        double totalRevenue = Double.parseDouble(lines[8].trim());

        return new BranchInventoryItem(itemId, branchId, productName, category, unitPrice,
                quantityInStock, totalBought, totalSold, totalRevenue);
    }

    @Override
    public void save(BranchInventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("cant save null item");
        }
        UUID itemId = item.getItemId();
        UUID branchId = item.getBranchId();
        Object lock = getLock(itemId);
        synchronized (lock) {
            String fileName = getFileName(itemId);
            if (fileExists(fileName)) {
                throw new IllegalArgumentException("cant save id already exists: " + itemId);
            }
            writeToFile(item, fileName);
            cache.put(itemId, item);
            updateBranchIndex(branchId, itemId, true);
        }
    }

    @Override
    public void delete(UUID itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("cant delete null item id");
        }
        Object lock = getLock(itemId);
        synchronized (lock) {
            BranchInventoryItem item = cache.get(itemId);
            if (item == null) {
                throw new IllegalArgumentException("cant delete item not found: " + itemId);
            }
            UUID branchId = item.getBranchId();
            String fileName = getFileName(itemId);
            deleteFile(fileName);
            cache.remove(itemId);
            updateBranchIndex(branchId, itemId, false);
        }
    }

    @Override
    public List<BranchInventoryItem> findAll() {
        List<BranchInventoryItem> items = new java.util.ArrayList<>();
        List<UUID> itemIds;
        synchronized (cache) {
            itemIds = new java.util.ArrayList<>(cache.keySet());
        }
        for (UUID itemId : itemIds) {
            Object lock = getLock(itemId);
            synchronized (lock) {
                BranchInventoryItem item = cache.get(itemId);
                if (item != null) {
                    items.add(item.createCopy());
                }
            }
        }
        return items;
    }

    @Override
    public Optional<BranchInventoryItem> findById(UUID itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("cant find null item id");
        }
        Object lock = getLock(itemId);
        synchronized (lock) {
            BranchInventoryItem item = cache.get(itemId);
            if (item == null) {
                return Optional.empty();
            }
            return Optional.of(item.createCopy());
        }
    }

    @Override
    public List<BranchInventoryItem> findByBranchId(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("cant find items, branch id cant be null");
        }
        List<BranchInventoryItem> items = new java.util.ArrayList<>();

        List<UUID> itemIds;
        synchronized (branchIndex) {
            itemIds = branchIndex.getOrDefault(branchId, new java.util.ArrayList<>());
            itemIds = new java.util.ArrayList<>(itemIds);
        }

        for (UUID itemId : itemIds) {
            Object lock = getLock(itemId);
            synchronized (lock) {
                BranchInventoryItem item = cache.get(itemId);
                if (item != null && item.getBranchId().equals(branchId)) {
                    items.add(item.createCopy());
                }
            }
        }
        return items;
    }

    @Override
    public void update(BranchInventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("cant update null item");
        }
        UUID itemId = item.getItemId();
        Object lock = getLock(itemId);
        synchronized (lock) {
            BranchInventoryItem existingItem = cache.get(itemId);
            if (existingItem == null) {
                throw new IllegalArgumentException("cant update item not found");
            }
            if (!existingItem.getBranchId().equals(item.getBranchId())) {
                throw new IllegalArgumentException(
                        "cant update, change branchId for existing item is not allowed, item id: " + itemId);
            }

            String fileName = getFileName(itemId);
            writeToFile(item, fileName);
            cache.put(itemId, item.createCopy());
        }
    }

    private void loadCache() {
        synchronized (cache) {
            cache.clear();
            branchIndex.clear();
            List<BranchInventoryItem> allItems = readAllFromDirectory();
            for (BranchInventoryItem item : allItems) {
                UUID itemId = item.getItemId();
                UUID branchId = item.getBranchId();
                cache.put(itemId, item);
                updateBranchIndex(branchId, itemId, true);
            }
        }
    }

    private void updateBranchIndex(UUID branchId, UUID itemId, boolean add) {
        synchronized (branchIndex) {
            List<UUID> itemIds = branchIndex.getOrDefault(branchId, new java.util.ArrayList<>());
            if (add) {
                if (!itemIds.contains(itemId)) {
                    itemIds.add(itemId);
                }
            } else {
                itemIds.remove(itemId);
                if (itemIds.isEmpty()) {
                    branchIndex.remove(branchId);
                }
            }
            branchIndex.put(branchId, itemIds);
        }
    }
}
