package server.application.adaptors;

import server.domain.Branch;
import server.domain.BranchInventoryItem;
import server.domain.employee.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchService {
    Optional<Branch> getBranch(UUID branchId);

    List<BranchInventoryItem> getBranchItems(UUID branchId);

    List<Employee> getBranchEmployees(UUID branchId);
}
