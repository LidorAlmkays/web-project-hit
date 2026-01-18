package frontend.cli.auth;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.LoginEmployeeRequest;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.Scanner;

public class LoginController {
    private final Gson gson = new Gson();

    public EmployeeDto login(IClientTransport client, Scanner scanner) throws IOException {
        System.out.println("=== Login ===");
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            SocketMessage response = client.send(EventType.LOGIN_EMPLOYEE, new LoginEmployeeRequest(email, password));
            if (response == null) {
                System.out.println("No response from server. Try again.");
            } else if (response.getData() instanceof String) {
                System.out.println("Login failed: " + response.getData());
            } else {
                System.out.println("Login successful. Welcome, " + email + "!\n");
                EmployeeDto request;
                if (response.getData() instanceof LoginEmployeeRequest) {
                    request = (EmployeeDto) response.getData();
                } else {
                    request = gson.fromJson(gson.toJson(response.getData()), EmployeeDto.class);
                }
                return request;

            }
            attempts++;
        }
        return null;
    }
}
