package server.infustructre.adaptors;

import server.domain.Branch;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository {
    void save(Branch branch);

    void update(Branch branch);

    void delete(UUID branchId);

    Optional<Branch> findById(UUID branchId);
}
