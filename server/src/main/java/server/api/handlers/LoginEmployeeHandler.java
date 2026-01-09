package server.api.handlers;

import server.application.adaptors.AuthService;
import server.domain.employee.Employee;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.LoginEmployeeRequest;

import java.net.Socket;

/**
 * Handles login requests from clients.
 */
public class LoginEmployeeHandler extends AbstractSocketHandler {
    private final AuthService authService;

    public LoginEmployeeHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String email = null;
        String password = null;
        try {
            LoginEmployeeRequest request;
            if (data instanceof LoginEmployeeRequest) {
                request = (LoginEmployeeRequest) data;
            } else {
                request = gson.fromJson(gson.toJson(data), LoginEmployeeRequest.class);
            }

            email = request.getEmail();
            password = request.getPassword();

            if (email == null || email.trim().isEmpty() || password == null) {
                sendError(clientSocket, "Missing email or password");
                return;
            }

            Employee employee = authService.login(email.trim(), password, clientSocket);
            sendSuccess(clientSocket, employee.toDto());
        } catch (IllegalArgumentException | SecurityException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.LOGIN_EMPLOYEE, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.LOGIN_EMPLOYEE, message));
    }
}
