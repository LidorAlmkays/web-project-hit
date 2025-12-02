package server;

import server.infustructre.InfrastructureFactory;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import server.infustructre.adaptors.BranchRepository;
import server.infustructre.adaptors.CustomerRepository;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;
import server.api.SocketServer;
import server.application.ApplicationFactory;
import server.application.adaptors.AuthService;
import server.application.adaptors.BranchService;
import server.application.adaptors.EmployeeService;
import server.application.adaptors.LoggerService;
import server.application.adaptors.UserManagementService;

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
        System.out.println("Creating application");
        UserManagementService userManagementService = applicationFactory.createUserManagementService(logRepository);
        EmployeeService employeeService = applicationFactory.createEmployeeService(employeeRepository, branchRepository,
                logRepository);
        BranchService branchService = applicationFactory.createBranchService(branchRepository,
                branchInventoryItemRepository, employeeRepository, logRepository);
        LoggerService logService = applicationFactory.createLoggerService(logRepository);
        AuthService authService = applicationFactory.createAuthService(employeeRepository, logRepository,
                userManagementService);
        System.out.println("Starting API");
        SocketServer socketServer = new SocketServer(authService, logService, employeeService);
        socketServer.start();

    }

}
