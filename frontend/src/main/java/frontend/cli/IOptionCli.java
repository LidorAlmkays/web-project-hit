package frontend.cli;

import frontend.transport.IClientTransport;
import java.io.IOException;
import java.util.Scanner;

public interface IOptionCli {
    String getOptionName();

    CliResult run(IClientTransport client, Scanner scanner) throws IOException;
}
