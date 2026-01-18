package frontend.cli.customermanagement;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.Scanner;

public class CustomerManagementCli implements IOptionCli {
    @Override
    public String getOptionName() {
        return "Customer management";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        CustomerManagementView view = new CustomerManagementView();
        CustomerManagementController controller = new CustomerManagementController(client, view, scanner);
        controller.run();
        return CliResult.BACK;
    }
}
