package server.application;

import server.application.adaptors.AuthService;
import server.application.adaptors.EmployeeService;
import server.application.services.AuthServiceImpl;
import server.application.services.EmployeeServiceImpl;
import server.infustructre.InfrastructureFactory;

public class ApplicationFactory {

    private final InfrastructureFactory infraFactory;

    public ApplicationFactory() {
        this.infraFactory = new InfrastructureFactory();
    }

    public AuthService createAuthService() {
        return new AuthServiceImpl(
                infraFactory.createEmployeeRepository(),
                infraFactory.createLogRepository());
    }

    public EmployeeService createEmployeeService() {
        return new EmployeeServiceImpl(
                infraFactory.createEmployeeRepository(),
                infraFactory.createBranchRepository(),
                infraFactory.createLogRepository());
    }
}
