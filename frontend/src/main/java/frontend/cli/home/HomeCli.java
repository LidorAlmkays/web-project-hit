package frontend.cli.home;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class HomeCli {
    private final List<IOptionCli> options;

    public HomeCli(List<IOptionCli> options) {
        this.options = options;
    }

    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        HomeView view = new HomeView();
        HomeController controller = new HomeController(client, view, scanner, options);
        return controller.run();
    }
}
