package server.application.adaptors;

import server.domain.BranchInventoryItem;

import java.util.List;
import java.util.UUID;

public interface BranchItemService {
    List<BranchInventoryItem> getBranchItems(UUID branchId);

    BranchInventoryItem buyItem(UUID branchId, UUID itemId, UUID customerId, int quantity);

    BranchInventoryItem restockItem(UUID branchId, UUID itemId, int quantity);
}
