package server.Api;

import server.application.adaptors.AuthService;
import server.application.adaptors.EmployeeService;
import server.application.adaptors.LoggerService;

public class SocketServer {
    private final AuthService authService;
    private final LoggerService logService;
    private final EmployeeService employeeService;

    public SocketServer(AuthService authService, LoggerService logService, EmployeeService employeeService) {
        this.authService = authService;
        this.logService = logService;
        this.employeeService = employeeService;
    }

    public void start() {
        System.out.println("Starting socket manager");
    }

    public void stop() {
        System.out.println("Stopping socket manager");
    }
}
