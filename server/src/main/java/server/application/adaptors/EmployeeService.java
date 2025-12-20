package server.application.adaptors;

import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {
    Employee createEmployee(UUID branchId, String fullName, String employeeId, String phoneNumber,
            String bankAccountNumber, EmployeeRole role, String email, String password);

    Optional<Employee> getEmployee(UUID employeeNumber);

    Employee updateEmployee(Employee employeeToUpdate);

    void deleteEmployee(UUID employeeNumber);
}
