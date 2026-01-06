package server.infustructre.adaptors;

import server.domain.BranchInventoryItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchInventoryItemRepository {
    void save(BranchInventoryItem item);

    void update(BranchInventoryItem item);

    void delete(UUID itemId);

    List<BranchInventoryItem> findAll();

    Optional<BranchInventoryItem> findById(UUID itemId);

    List<BranchInventoryItem> findByBranchId(UUID branchId);
}
