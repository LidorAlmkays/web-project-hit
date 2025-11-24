package server.application.services;

import server.application.adaptors.EmployeeService;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;
import server.infustructre.adaptors.BranchRepository;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final LogRepository logRepository;
    private final Map<UUID, Object> employeeLocks = new ConcurrentHashMap<>();
    private final Object creationMutex = new Object();

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BranchRepository branchRepository,
            LogRepository logRepository) {
        this.employeeRepository = employeeRepository;
        this.branchRepository = branchRepository;
        this.logRepository = logRepository;
    }

    private Object getEmployeeLock(UUID employeeNumber) {
        Object lock = employeeLocks.get(employeeNumber);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = employeeLocks.get(employeeNumber);
                if (lock == null) {
                    lock = new Object();
                    employeeLocks.put(employeeNumber, lock);
                }
            }
        }
        return lock;
    }

    @Override
    public Employee createEmployee(UUID branchId, String fullName, String employeeId, String phoneNumber,
            String bankAccountNumber, EmployeeRole role, String email, String password) {
        if (role != EmployeeRole.ADMIN && branchId != null) {
            try {
                if (branchRepository.findById(branchId).isEmpty()) {
                    Error error = new Error("create employee failed, branch not found: " + branchId);
                    logRepository.error(error);
                    throw new IllegalArgumentException(error);
                }
            } catch (Exception e) {
                Error error = new Error("create employee error, when trying to find branch: " + e.getMessage());
                logRepository.error(error);
                throw new RuntimeException(error);
            }
        }

        Employee newEmployee = new Employee(branchId, fullName, employeeId, phoneNumber, bankAccountNumber, role, email,
                password);

        try {
            employeeRepository.save(newEmployee);
            logRepository.info("new employee created: " + newEmployee.getEmployeeNumber());
            return newEmployee;
        } catch (Exception e) {
            Error error = new Error("create employee error, when trying to save employee: " + e.getMessage());
            logRepository.error(error);
            throw new RuntimeException(error);
        }
    }

    @Override
    public Optional<Employee> getEmployee(UUID employeeNumber) {
        try {
            return employeeRepository.findByEmployeeNumber(employeeNumber);
        } catch (Exception e) {
            Error error = new Error(
                    "get employee error, when trying to find employee: " + employeeNumber + ", " + e.getMessage());
            logRepository.error(error);
            return Optional.empty();
        }
    }

    @Override
    public Employee updateEmployee(Employee employeeToUpdate) {
        UUID employeeNumber = employeeToUpdate.getEmployeeNumber();
        Object lock = getEmployeeLock(employeeNumber);

        synchronized (lock) {
            Optional<Employee> existingEmployeeOpt = getEmployee(employeeNumber);
            if (existingEmployeeOpt.isEmpty()) {
                Error error = new Error("update failed, employee not found: " + employeeNumber);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            try {
                employeeRepository.update(employeeToUpdate);
                logRepository.info("employee updated: " + employeeNumber);
                return employeeToUpdate;
            } catch (Exception e) {
                Error error = new Error(
                        "employee update error, when trying to update into repository: " + employeeNumber
                                + ", " + e.getMessage());
                logRepository.error(error);
                throw new RuntimeException(error);
            }
        }
    }

    @Override
    public void deleteEmployee(UUID employeeNumber) {
        Object lock = getEmployeeLock(employeeNumber);
        synchronized (lock) {
            if (getEmployee(employeeNumber).isEmpty()) {
                Error error = new Error("delete failed, employee not found: " + employeeNumber);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            try {
                employeeRepository.delete(employeeNumber);
                logRepository.info("employee deleted: " + employeeNumber);
            } catch (Exception e) {
                Error error = new Error("employee delete error, when trying to delete from repository: "
                        + employeeNumber + ", " + e.getMessage());
                logRepository.error(error);
                throw new RuntimeException(error);
            }
        }
    }
}
