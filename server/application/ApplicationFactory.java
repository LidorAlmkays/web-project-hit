package server.application;

import server.application.adaptors.AuthService;
import server.application.adaptors.BranchService;
import server.application.adaptors.EmployeeService;
import server.application.adaptors.LoggerService;
import server.application.adaptors.UserManagementService;
import server.application.services.AuthServiceImpl;
import server.application.services.BranchServiceImpl;
import server.application.services.EmployeeServiceImpl;
import server.application.services.LoggerServiceImpl;
import server.application.services.UserManagementServiceImpl;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import server.infustructre.adaptors.BranchRepository;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;

public class ApplicationFactory {

    public ApplicationFactory() {
    }

    public AuthService createAuthService(EmployeeRepository employeeRepository, LogRepository logRepository,
            UserManagementService userManagementService) {
        return new AuthServiceImpl(
                employeeRepository,
                logRepository,
                userManagementService);
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

    public BranchService createBranchService(BranchRepository branchRepository,
            BranchInventoryItemRepository branchInventoryItemRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        return new BranchServiceImpl(
                branchRepository,
                branchInventoryItemRepository,
                employeeRepository,
                logRepository);
    }

    public UserManagementService createUserManagementService(LogRepository logRepository) {
        return new UserManagementServiceImpl(logRepository);
    }
}
