package server.api.handlers;

import server.domain.employee.Employee;
import server.infustructre.persistentTxtStorage.FileEmployeeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeRepositoryReader extends FileEmployeeRepository {
    public List<Employee> listAll() {
        return readAllFromDirectory();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String target = email.trim();
        for (Employee employee : listAll()) {
            if (target.equals(employee.getEmail())) {
                return Optional.of(employee);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> findByBranchId(UUID branchId) {
        if (branchId == null) {
            return new ArrayList<>();
        }
        List<Employee> matches = new ArrayList<>();
        for (Employee employee : listAll()) {
            if (branchId.equals(employee.getBranchId())) {
                matches.add(employee);
            }
        }
        return matches;
    }
}
