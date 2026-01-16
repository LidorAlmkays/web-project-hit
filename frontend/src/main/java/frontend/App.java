package frontend;

import frontend.cli.CliResult;
import frontend.cli.auth.LoginController;
import frontend.cli.chat.ChatManagementCli;
import frontend.cli.employeemanagement.EmployeeManagementCli;
import frontend.cli.customermanagement.CustomerManagementCli;

import frontend.cli.storagemanagement.StorageManagementConsole;
import frontend.cli.home.HomeCli;
import frontend.transport.IClientTransport;
import frontend.transport.SocketClient;
import frontend.util.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "127.0.0.1";

    public static void main(String[] args) {
        try (IClientTransport client = new SocketClient(DEFAULT_HOST, DEFAULT_PORT); Scanner scanner = new Scanner(System.in)) {
            LoginController loginController = new LoginController();
            boolean repeat=true;
            do {
                shareddto.employeemanagement.response.EmployeeDto loggedInEmployee = loginController.login(client, scanner);
                if (loggedInEmployee == null) {
                    System.out.println("Login failed after multiple attempts. Exiting.");
                    return;
                }
                SessionManager.getInstance().setCurrentEmployee(loggedInEmployee);
                HomeCli homeCli = new HomeCli(List.of(
                        new EmployeeManagementCli(),
                        new CustomerManagementCli(),
                        new StorageManagementConsole(),
                        new ChatManagementCli()));
                CliResult result = homeCli.run(client, scanner);
                if (result == CliResult.EXIT) {
                 repeat = false;
                }
            } while (repeat);
            // exit after user chose Exit
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }
}
