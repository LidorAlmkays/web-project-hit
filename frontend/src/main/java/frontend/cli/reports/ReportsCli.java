package frontend.cli.reports;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.services.FrontendChatService;
import frontend.services.FrontendLoggerService;
import frontend.services.FrontendReportService;
import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.Scanner;

public class ReportsCli implements IOptionCli {
    @Override
    public String getOptionName() {
        return "Reports Management";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        FrontendReportService reportService = new FrontendReportService(client);
        FrontendLoggerService loggerService = new FrontendLoggerService(client);
        FrontendChatService chatService = new FrontendChatService(client);

        ReportController controller = new ReportController(reportService, loggerService, chatService);
        controller.showMenu();
        return CliResult.BACK;
    }
}
