package server;

import server.chat.ChatManager;
import server.infustructre.InfrastructureFactory;
import server.infustructre.adaptors.*;
import server.api.SocketServer;
import server.application.ApplicationFactory;
import server.application.adaptors.*;

public class App {
    private final InfrastructureFactory infrastructureFactory;
    private final ApplicationFactory applicationFactory;

    public App() {
        this.infrastructureFactory = new InfrastructureFactory();
        this.applicationFactory = new ApplicationFactory();
    }

    public void start() {
        System.out.println("Starting application");

        System.out.println("Creating infrastructure");
        CustomerRepository customerRepository = infrastructureFactory.createCustomerRepository();
        BranchRepository branchRepository = infrastructureFactory.createBranchRepository();
        BranchInventoryItemRepository branchInventoryItemRepository = infrastructureFactory
                .createBranchInventoryItemRepository();
        EmployeeRepository employeeRepository = infrastructureFactory.createEmployeeRepository();
        LogRepository logRepository = infrastructureFactory.createLogRepository();
        PasswordSettingsRepository passwordSettingsRepository = infrastructureFactory
                .createPasswordSettingsRepository();
        System.out.println("Creating application");
        UserManagementService userManagementService = applicationFactory
                .createUserManagementService(logRepository);
        PasswordSettingsService passwordSettingsService = applicationFactory
                .createPasswordSettingsService(passwordSettingsRepository);
        EmployeeService employeeService = applicationFactory.createEmployeeService(employeeRepository,
                branchRepository,
                logRepository,
                passwordSettingsService);
        BranchService branchService = applicationFactory.createBranchService(branchRepository,
                branchInventoryItemRepository, employeeRepository, logRepository);
        LoggerService logService = applicationFactory.createLoggerService(logRepository);
        ReportService reportService = applicationFactory.createReportService(logRepository, branchInventoryItemRepository);
        AuthService authService = applicationFactory.createAuthService(employeeRepository, logRepository,
                userManagementService);
        BranchItemService branchItemService = applicationFactory.createBranchItemService(branchRepository,
                branchInventoryItemRepository, customerRepository, logRepository);
        CustomerService customerService = applicationFactory.createCustomerService(customerRepository,
                logRepository);
        ChatManager.getInstance().setDependencies(userManagementService, employeeRepository, logRepository);
        System.out.println("Starting API");
        SocketServer socketServer = new SocketServer(authService, logService, employeeService,
                branchItemService, branchService, customerService, reportService, passwordSettingsService,
                employeeRepository);
        socketServer.start();

    }

}
