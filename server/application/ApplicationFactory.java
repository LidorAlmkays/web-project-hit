package server.application;

import server.application.adaptors.AuthService;
import server.application.adaptors.EmployeeService;
import server.application.services.AuthServiceImpl;
import server.application.services.EmployeeServiceImpl;
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
}
