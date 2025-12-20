package server.infustructre.adaptors;

import server.domain.employee.Employee;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {
    void save(Employee employee);

    void update(Employee employee);

    void delete(UUID employeeNumber);

    Optional<Employee> findByEmployeeNumber(UUID employeeNumber);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByBranchId(UUID branchId);
}
