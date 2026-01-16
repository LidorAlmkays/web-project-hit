package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.EmployeeCreateRequest;
import server.application.adaptors.EmployeeService;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;

import java.net.Socket;
import java.util.Locale;
import java.util.UUID;

public class CreateEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;

    public CreateEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            EmployeeCreateRequest request = gson.fromJson(gson.toJsonTree(data), EmployeeCreateRequest.class);
            UUID branchId = parseOptionalUuid(request.getBranchId());
            EmployeeRole role = parseRole(request.getRole());

            Employee employee = employeeService.createEmployee(
                    branchId,
                    request.getFullName(),
                    request.getEmployeeId(),
                    request.getPhoneNumber(),
                    request.getBankAccountNumber(),
                    role,
                    request.getEmail(),
                    request.getPassword());

            sendMessage(clientSocket,
                    new SocketMessage(EventType.CREATE_EMPLOYEE, EmployeeMapper.toDto(employee)));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.CREATE_EMPLOYEE, e.getMessage()));
        }
    }

    private UUID parseOptionalUuid(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return UUID.fromString(trimmed);
    }

    private EmployeeRole parseRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("role is required");
        }
        return EmployeeRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
    }

}
