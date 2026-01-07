package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import server.application.adaptors.EmployeeService;

import java.net.Socket;
import java.util.UUID;

public class DeleteEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;

    public DeleteEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            EmployeeDeleteRequest request = gson.fromJson(gson.toJsonTree(data), EmployeeDeleteRequest.class);
            UUID employeeNumber = parseRequiredUuid(request.getEmployeeNumber(), "employeeNumber is required");
            employeeService.deleteEmployee(employeeNumber);
            sendMessage(clientSocket, new SocketMessage(EventType.DELETE_EMPLOYEE, true));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.DELETE_EMPLOYEE, e.getMessage()));
        }
    }

    private UUID parseRequiredUuid(String raw, String errorMessage) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return UUID.fromString(raw.trim());
    }

    private static class EmployeeDeleteRequest {
        private String employeeNumber;

        public String getEmployeeNumber() {
            return employeeNumber;
        }
    }
}
