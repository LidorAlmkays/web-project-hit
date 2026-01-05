package frontend;

import frontend.cli.auth.LoginController;
import frontend.cli.employeemanagement.EmployeeManagementCli;
import frontend.cli.employeemanagement.EmployeeManagementController;
import frontend.transport.IClientTransport;
import frontend.transport.SocketClient;
import frontend.transport.mock.MockSocketClient;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "127.0.0.1";

    public static void main(String[] args) {
        List<String> params = new ArrayList<>();
        boolean offline = false;
        for (String arg : args) {
            if ("--offline".equalsIgnoreCase(arg) || "--mock".equalsIgnoreCase(arg)) {
                offline = true;
            } else {
                params.add(arg);
            }
        }

        String host = params.size() > 0 ? params.get(0) : DEFAULT_HOST;
        int port = params.size() > 1 ? Integer.parseInt(params.get(1)) : DEFAULT_PORT;

        try (IClientTransport client = createClient(offline, host, port); Scanner scanner = new Scanner(System.in)) {
            LoginController loginController = new frontend.cli.auth.LoginController();
            boolean repeat;
            do {
                EmployeeDto loggedInEmployee = loginController.login(client, scanner);
                if (loggedInEmployee == null) {
                    System.out.println("Login failed after multiple attempts. Exiting.");
                    return;
                }

                EmployeeManagementController.ControllerResult result = new EmployeeManagementCli().run(client, scanner, loggedInEmployee);
                if (result == EmployeeManagementController.ControllerResult.LOGGED_OUT) {
                    System.out.println("You have been logged out.");
                    repeat = true;
                } else {
                    repeat = false;
                }
            } while (repeat);
            // exit after user chose Exit
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    /**
     * Creates a transport based on CLI flags (mock or socket).
     */
    private static IClientTransport createClient(boolean offline, String host, int port) throws IOException {
        if (offline) {
            System.out.println("Running in mock mode (no server connection).");
            return new MockSocketClient();
        }
        return new SocketClient(host, port);
    }
}
