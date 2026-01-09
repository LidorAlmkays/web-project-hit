package frontend.cli.home;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.LogoutEmployeeRequest;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HomeController {
    private final IClientTransport client;
    private final HomeView view;
    private final Scanner scanner;
    private final List<IOptionCli> options;

    public HomeController(IClientTransport client, HomeView view, Scanner scanner, List<IOptionCli> options) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
        this.options = options;
    }

    public CliResult run() throws IOException {
        List<String> optionNames = options.stream()
                .map(IOptionCli::getOptionName)
                .collect(Collectors.toList());

        while (true) {
            view.menu(optionNames);
            if (!scanner.hasNextLine()) {
                return CliResult.EXIT;
            }
            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                view.error("Invalid input.");
                continue;
            }

            if (choice > 0 && choice <= options.size()) {
                IOptionCli selectedCli = options.get(choice - 1);
                CliResult result = selectedCli.run(client, scanner);
                if (result == CliResult.LOGOUT) {
                    return CliResult.LOGOUT;
                }
                if (result == CliResult.EXIT) {
                    return CliResult.EXIT;
                }
                // If BACK, loop continues
            } else if (choice == options.size() + 1) {
                // Logout
                logout();
                return CliResult.BACK;
            } else if (choice == options.size() + 2) {
                // Exit
                return CliResult.EXIT;
            } else {
                view.error("Unknown option.");
            }
        }
    }

   private void logout() throws IOException {
        SessionManager session = SessionManager.getInstance();
        EmployeeDto currentEmployee = session.getCurrentEmployee();

        if (currentEmployee == null) {
            view.error("No logged-in user information available");
            return;
        }
        LogoutEmployeeRequest request = new LogoutEmployeeRequest(currentEmployee.getEmployeeNumber());
        SocketMessage response = client.send(EventType.LOGOUT_EMPLOYEE, request);
        if (response == null) {
            return;
        }
        session.setCurrentEmployee(null);
        view.info("Logged out.");
    }
}
