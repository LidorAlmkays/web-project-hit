package frontend.cli.employeemanagement;

import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.Scanner;

/**
 * CLI entry point for employee management tasks.
 */
public class EmployeeManagementCli {
    /**
     * Runs the employee management CLI with the given transport and input source.
     */
    public void run(IClientTransport client, Scanner scanner) throws IOException {
        EmployeeManagementView view = new EmployeeManagementView();
        EmployeeManagementController controller = new EmployeeManagementController(client, view, scanner);
        controller.run();
    }
}
