package server.application.services;

import server.application.adaptors.AuthService;
import server.domain.employee.Employee;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;

import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;

    private final Map<UUID, Socket> activeSessions = new ConcurrentHashMap<>();

    public AuthServiceImpl(EmployeeRepository employeeRepository, LogRepository logRepository) {
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
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

        synchronized (this) {
            if (activeSessions.containsKey(employee.getEmployeeNumber())) {
                Error error = new Error(
                        "login failed: Employee " + employee.getEmployeeNumber() + " is already logged in");
                logRepository.error(error);
                throw new SecurityException(error);
            }
            activeSessions.put(employee.getEmployeeNumber(), socket);
        }

        logRepository.info("Login successful: Employee " + employee.getEmployeeNumber() + " logged in");
        return employee;
    }

    @Override
    public void logout(UUID employeeNumber) {
        if (employeeNumber == null) {
            return;
        }

        try {
            if (activeSessions.remove(employeeNumber) != null) {
                logRepository.info("logout successful: Employee " + employeeNumber + " logged out");
            } else {
                logRepository.info("Logout failed, employee wasnt loged in: " + employeeNumber);
            }
        } catch (Exception e) {
            logRepository.error(new Error(
                    "logout error, removing session for employee: " + employeeNumber + ", " + e.getMessage()));
        }
    }
}
