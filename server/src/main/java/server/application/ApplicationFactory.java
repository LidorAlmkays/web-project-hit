package server.application;

import server.application.adaptors.*;
import server.application.services.*;
import server.infustructre.adaptors.*;

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

    public BranchItemService createBranchItemService(BranchRepository branchRepository,
            BranchInventoryItemRepository branchInventoryItemRepository,
            CustomerRepository customerRepository,
            LogRepository logRepository) {
        return new BranchItemServiceImpl(
                branchRepository,
                branchInventoryItemRepository,
                customerRepository,
                logRepository);
    }

    public UserManagementService createUserManagementService(LogRepository logRepository) {
        return new UserManagementServiceImpl(logRepository);
    }

    public CustomerService createCustomerService(CustomerRepository customerRepository, LogRepository logRepository) {
        return new CustomerServiceImpl(customerRepository, logRepository);
    }
}
