package server.application.services;

import server.application.adaptors.AuthService;
import server.application.adaptors.UserManagementService;
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
                    "login error, when trying to finding employee by email: " + email + ", error: " + e.getMessage());
            logRepository.error(error);
            throw new RuntimeException(error);
        }

        if (employeeOpt.isEmpty()) {
            Error error = new Error("login failed: No employee found with email: " + email);
            logRepository.error(error);
            throw new IllegalArgumentException("user not found");
        }

        Employee employee = employeeOpt.get();

        if (!employee.getPassword().equals(password)) {
            Error error = new Error("login failed: incorrect password for employee " + employee.getEmployeeNumber());
            logRepository.error(error);
            throw new IllegalArgumentException("login failed, invalid credentials");
        }

        // Check if user is already logged in
        if (userManagementService.getSocketByEmail(email).isPresent()) {
            Error error = new Error("login failed: Employee " + email + " is already logged in");
            logRepository.error(error);
            throw new SecurityException(error);
        }
        userManagementService.addUser(email, employee, socket);

        logRepository.info("Login successful: Employee " + employee.getEmployeeNumber() + " logged in");
        return employee;
    }

    @Override
    public void logout(UUID employeeNumber) {
        if (employeeNumber == null) {
            return;
        }

        try {
            // Find employee by employeeNumber to get email
            Optional<Employee> employeeOpt = employeeRepository.findByEmployeeNumber(employeeNumber);
            if (employeeOpt.isPresent()) {
                String email = employeeOpt.get().getEmail();
                userManagementService.removeUser(email);
                logRepository.info("logout successful: Employee " + employeeNumber + " logged out");
            } else {
                logRepository.info("Logout failed, employee not found: " + employeeNumber);
            }
        } catch (Exception e) {
            logRepository.error(new Error(
                    "logout error, removing session for employee: " + employeeNumber + ", " + e.getMessage()));
        }
    }
}
