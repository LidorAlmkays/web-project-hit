package server.api.handlers;

import server.application.adaptors.AuthService;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.LogoutEmployeeRequest;

import java.net.Socket;
import java.util.UUID;

/**
 * Handles logout requests from clients.
 */
public class LogoutEmployeeHandler extends AbstractSocketHandler {
    private final AuthService authService;

    public LogoutEmployeeHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            LogoutEmployeeRequest req = gson.fromJson(gson.toJson(data), LogoutEmployeeRequest.class);
            String employeeNumberStr = req == null ? null : req.getEmployeeNumber();

            if (employeeNumberStr == null || employeeNumberStr.trim().isEmpty()) {
                sendError(clientSocket, "Missing employee number");
                return;
            }

            UUID employeeNumber = UUID.fromString(employeeNumberStr.trim());
            authService.logout(employeeNumber);
            sendSuccess(clientSocket);
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.LOGOUT_EMPLOYEE, null));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.LOGOUT_EMPLOYEE, message));
    }
}
