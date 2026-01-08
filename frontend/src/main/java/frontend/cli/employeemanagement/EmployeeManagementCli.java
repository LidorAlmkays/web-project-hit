package frontend.cli.employeemanagement;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.Scanner;

/**
 * CLI entry point for employee management tasks.
 */
public class EmployeeManagementCli implements IOptionCli {
    @Override
    public String getOptionName() {
        return "Employee management";
    }

    /**
     * Runs the employee management CLI with the given transport and input source.
     */
    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        EmployeeManagementView view = new EmployeeManagementView();
        EmployeeManagementController controller = new EmployeeManagementController(client, view, scanner);
        controller.run();
        return CliResult.BACK;
    }
}
