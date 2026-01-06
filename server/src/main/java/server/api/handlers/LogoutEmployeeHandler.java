package server.api.handlers;

import com.google.gson.Gson;
import shareddto.EventType;
import shareddto.SocketMessage;
import server.application.adaptors.AuthService;
import shareddto.employeemanagement.request.LogoutEmployeeRequest;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;

/**
 * Handles logout requests from clients.
 */
public class LogoutEmployeeHandler implements SocketHandler {
    private final AuthService authService;
    private final Gson gson = new Gson();

    public LogoutEmployeeHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            LogoutEmployeeRequest req = gson.fromJson(gson.toJson(data),
                    LogoutEmployeeRequest.class);
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

    private void sendMessage(Socket clientSocket, SocketMessage message) throws Exception {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeUTF(gson.toJson(message));
        out.flush();
    }
}
