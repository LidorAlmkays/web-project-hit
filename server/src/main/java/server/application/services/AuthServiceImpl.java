package server.application.services;

import server.application.adaptors.AuthService;
import server.application.adaptors.UserManagementService;
import server.domain.LogEntry;
import server.domain.employee.Employee;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;

import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;
    private final UserManagementService userManagementService;

    public AuthServiceImpl(EmployeeRepository employeeRepository, LogRepository logRepository,
            UserManagementService userManagementService) {
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
        this.userManagementService = userManagementService;
    }

    @Override
    public Employee login(String email, String password, Socket socket) {
        Optional<Employee> employeeOpt;
        try {
            employeeOpt = employeeRepository.findByEmail(email);
        } catch (Exception e) {
            Error error = new Error(
                    "[LOGIN] error, when trying to finding employee by email: " + email + ", error: " + e.getMessage());
            logRepository.error(LogEntry.LogType.AUTHENTICATION, email, error.getMessage());
            throw new RuntimeException(error);
        }

        if (employeeOpt.isEmpty()) {
            Error error = new Error("[LOGIN] failed: No employee found with email: " + email);
            logRepository.error(LogEntry.LogType.AUTHENTICATION, email, error.getMessage());
            throw new IllegalArgumentException("user not found");
        }

        Employee employee = employeeOpt.get();

        if (!employee.getPassword().equals(password)) {
            Error error = new Error("[LOGIN] failed: incorrect password for employee " + employee.getEmployeeNumber());
            logRepository.error(LogEntry.LogType.AUTHENTICATION, email, error.getMessage());
            throw new IllegalArgumentException("[LOGIN] failed, invalid credentials");
        }

        if (userManagementService.getSocketByEmail(email).isPresent()) {
            Error error = new Error("[LOGIN] failed: Employee " + email + " is already logged in");
            logRepository.error(LogEntry.LogType.AUTHENTICATION, email, error.getMessage());
            throw new SecurityException(error);
        }
        userManagementService.addUser(email, employee, socket);

        logRepository.info(LogEntry.LogType.AUTHENTICATION, email, "[LOGIN] successful: Employee " + employee.getEmployeeNumber() + " logged in");
        return employee;
    }

    @Override
    public void logout(UUID employeeNumber) {
        if (employeeNumber == null) {
            return;
        }

        try {
            Optional<Employee> employeeOpt = employeeRepository.findByEmployeeNumber(employeeNumber);
            if (employeeOpt.isPresent()) {
                String email = employeeOpt.get().getEmail();
                userManagementService.removeUser(email);
                logRepository.info(LogEntry.LogType.AUTHENTICATION, email, "[LOGOUT] successful: Employee " + employeeNumber + " logged out");
            } else {
                logRepository.info(LogEntry.LogType.AUTHENTICATION, "[LOGOUT] failed: Employee not found: " + employeeNumber);
            }
        } catch (Exception e) {
            logRepository.error(LogEntry.LogType.AUTHENTICATION, 
                    "[LOGOUT] error, removing session for employee: " + employeeNumber + ", " + e.getMessage());
        }
    }
}