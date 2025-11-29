package server.application;

import server.application.adaptors.AuthService;
import server.application.adaptors.EmployeeService;
import server.application.adaptors.LoggerService;
import server.application.services.AuthServiceImpl;
import server.application.services.EmployeeServiceImpl;
import server.application.services.LoggerServiceImpl;
import server.infustructre.adaptors.BranchRepository;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;

public class ApplicationFactory {

    public ApplicationFactory() {
    }

    public AuthService createAuthService(EmployeeRepository employeeRepository, LogRepository logRepository) {
        return new AuthServiceImpl(
                employeeRepository,
                logRepository);
    }

    public EmployeeService createEmployeeService(EmployeeRepository employeeRepository,
            BranchRepository branchRepository, LogRepository logRepository) {
        return new EmployeeServiceImpl(
                employeeRepository,
                branchRepository,
                logRepository);
    }

    public LoggerService createLoggerService(LogRepository logRepository) {
        return new LoggerServiceImpl(logRepository);
    }
}
