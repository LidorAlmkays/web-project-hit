package frontend.cli.adminmanagement;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.Scanner;

public class AdminManagementCli implements IOptionCli {
    @Override
    public String getOptionName() {
        return "Admin management";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        AdminManagementView view = new AdminManagementView();
        AdminManagementController controller = new AdminManagementController(client, view, scanner);
        controller.run();
        return CliResult.BACK;
    }
}
