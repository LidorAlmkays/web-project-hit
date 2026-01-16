package server.application.services;

import server.application.adaptors.EmployeeService;
import server.application.adaptors.PasswordSettingsService;
import server.domain.PasswordSettings;
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
    private final PasswordSettingsService passwordSettingsService;
    private final Map<UUID, Object> employeeLocks = new ConcurrentHashMap<>();
    private final Object creationMutex = new Object();

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BranchRepository branchRepository,
            LogRepository logRepository, PasswordSettingsService passwordSettingsService) {
        if (passwordSettingsService == null) {
            throw new IllegalArgumentException("passwordSettingsService must not be null");
        }
        this.employeeRepository = employeeRepository;
        this.branchRepository = branchRepository;
        this.logRepository = logRepository;
        this.passwordSettingsService = passwordSettingsService;
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
        
       validatePassword(password);
        
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

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            Error error = new Error("Password validation failed: password is null or empty");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        PasswordSettings settings = passwordSettingsService.getPasswordSettings();
        logRepository.info("Validating password against settings: passwordlength8=" + settings.isPasswordlength8()
                + ", oneUpperletter=" + settings.isOneUpperletter() + ", oneNumber=" + settings.isOneNumber());

        StringBuilder validationErrors = new StringBuilder();

        if (settings.isPasswordlength8() && password.length() < 8) {
            validationErrors.append("Password must be at least 8 characters long. ");
            logRepository.info("Password validation failed: length requirement not met (length=" + password.length() + ")");
        }

        if (settings.isOneUpperletter() && !hasUpperCaseLetter(password)) {
            validationErrors.append("Password must contain at least one uppercase letter. ");
            logRepository.info("Password validation failed: uppercase letter requirement not met");
        }

        if (settings.isOneNumber() && !hasNumber(password)) {
            validationErrors.append("Password must contain at least one number. ");
            logRepository.info("Password validation failed: number requirement not met");
        }

        if (validationErrors.length() > 0) {
            Error error = new Error("Password validation failed: " + validationErrors.toString().trim());
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        logRepository.info("Password validation passed for employee creation");
    }

    private boolean hasUpperCaseLetter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNumber(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
}
