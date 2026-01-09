package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.EmployeeDeleteRequest;
import server.application.adaptors.EmployeeService;

import java.net.Socket;
import java.util.UUID;

public class DeleteEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;
    private final EmployeeRepositoryReader employeeRepositoryReader;

    public DeleteEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.employeeRepositoryReader = new EmployeeRepositoryReader();
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            EmployeeDeleteRequest request = gson.fromJson(gson.toJsonTree(data), EmployeeDeleteRequest.class);
            UUID employeeNumber = resolveEmployeeNumber(request);
            employeeService.deleteEmployee(employeeNumber);
            sendMessage(clientSocket, new SocketMessage(EventType.DELETE_EMPLOYEE, true));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.DELETE_EMPLOYEE, e.getMessage()));
        }
    }

    private UUID resolveEmployeeNumber(EmployeeDeleteRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        return employeeRepositoryReader.findByEmail(request.getEmail().trim())
                .map(employee -> employee.getEmployeeNumber())
                .orElseThrow(() -> new IllegalArgumentException("employee not found: " + request.getEmail().trim()));
    }

}
