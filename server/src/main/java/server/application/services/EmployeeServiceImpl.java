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
        this.logRepository
                .info(LogEntry.LogType.MANAGEMENT, "[CREATE EMPLOYEE] creating employee: " + fullName + " " + employeeId + " " + phoneNumber + " " + bankAccountNumber
                        + " " + role + " " + email + " " + password);
        if (role != EmployeeRole.ADMIN && branchId != null) {
            try {
                if (branchRepository.findById(branchId).isEmpty()) {
                    Error error = new Error("[CREATE EMPLOYEE] failed, branch not found: " + branchId);
                    logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                    throw new IllegalArgumentException(error);
                }
            } catch (Exception e) {
                Error error = new Error("[CREATE EMPLOYEE] error, when trying to find branch: " + e.getMessage());
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new RuntimeException(error);
            }
        }

        Employee newEmployee = new Employee(branchId, fullName, employeeId, phoneNumber, bankAccountNumber, role, email,
                password);

        try {
            employeeRepository.save(newEmployee);
            logRepository.info(LogEntry.LogType.MANAGEMENT, email, "[CREATE EMPLOYEE] new employee created: " + newEmployee.getEmployeeNumber());
            return newEmployee;
        } catch (Exception e) {
            Error error = new Error("[CREATE EMPLOYEE] error, when trying to save employee: " + e.getMessage());
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new RuntimeException(error);
        }
    }

    @Override
    public Optional<Employee> getEmployee(String email) {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET EMPLOYEE] getting employee by email: " + email);

        if (email == null || email.trim().isEmpty()) {
            Error error = new Error("[GET EMPLOYEE] failed, email is null or empty");
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Employee> employee = employeeRepository.findByEmail(email);
            if (employee.isPresent()) {
                logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET EMPLOYEE] employee found by email, email=" + email
                        + ", employeeNumber=" + employee.get().getEmployeeNumber());
            } else {
                logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET EMPLOYEE] employee not found by email, email=" + email);
            }
            return employee;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            Error error = new Error("[GET EMPLOYEE] error, when trying to find employee by email: " + email
                    + ", " + e.getMessage());
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Employee updateEmployee(Employee employeeToUpdate) {
        UUID employeeNumber = employeeToUpdate.getEmployeeNumber();
        Object lock = getEmployeeLock(employeeNumber);

        synchronized (lock) {
            Optional<Employee> existingEmployeeOpt = employeeRepository.findByEmployeeNumber(employeeNumber);
            if (existingEmployeeOpt.isEmpty()) {
                Error error = new Error("[UPDATE EMPLOYEE] failed, employee not found: " + employeeNumber);
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            try {
                employeeRepository.update(employeeToUpdate);
                logRepository.info(LogEntry.LogType.MANAGEMENT, "[UPDATE EMPLOYEE] employee updated: " + employeeNumber);
                return employeeToUpdate;
            } catch (Exception e) {
                Error error = new Error(
                        "[UPDATE EMPLOYEE] error, when trying to update into repository: " + employeeNumber
                                + ", " + e.getMessage());
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new RuntimeException(error);
            }
        }
    }

    @Override
    public void deleteEmployee(UUID employeeNumber) {
        Object lock = getEmployeeLock(employeeNumber);
        synchronized (lock) {
            Optional<Employee> existingEmployeeOpt = employeeRepository.findByEmployeeNumber(employeeNumber);
            if (existingEmployeeOpt.isEmpty()) {
                Error error = new Error("[DELETE EMPLOYEE] failed, employee not found: " + employeeNumber);
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            try {
                employeeRepository.delete(employeeNumber);
                logRepository.info(LogEntry.LogType.MANAGEMENT, "[DELETE EMPLOYEE] Successful for employee: " + employeeNumber);
            } catch (Exception e) {
                Error error = new Error("[DELETE EMPLOYEE] error, when trying to delete from repository: "
                        + employeeNumber + ", " + e.getMessage());
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new RuntimeException(error);
            }
        }
    }
}
