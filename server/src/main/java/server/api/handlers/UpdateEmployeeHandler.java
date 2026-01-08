package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.EmployeeUpdateRequest;
import server.application.adaptors.EmployeeService;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;

import java.net.Socket;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class UpdateEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;

    public UpdateEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            EmployeeUpdateRequest request = gson.fromJson(gson.toJsonTree(data), EmployeeUpdateRequest.class);
            Employee existing = resolveExisting(request);
            UUID employeeNumber = existing.getEmployeeNumber();
            UUID branchId = resolveBranchId(request.getBranchId(), existing.getBranchId());
            EmployeeRole role = resolveRole(request.getRole(), existing.getRole());

            Employee updated = new Employee(
                    employeeNumber,
                    branchId,
                    resolveString(request.getFullName(), existing.getFullName()),
                    resolveString(request.getEmployeeId(), existing.getEmployeeId()),
                    resolveString(request.getPhoneNumber(), existing.getPhoneNumber()),
                    resolveString(request.getBankAccountNumber(), existing.getBankAccountNumber()),
                    role,
                    resolveString(request.getEmail(), existing.getEmail()),
                    resolveString(request.getPassword(), existing.getPassword()));

            Employee saved = employeeService.updateEmployee(updated);
            sendMessage(clientSocket,
                    new SocketMessage(EventType.UPDATE_EMPLOYEE, EmployeeMapper.toDto(saved)));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_EMPLOYEE, e.getMessage()));
        }
    }

    private Employee resolveExisting(EmployeeUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Optional<Employee> existing = employeeService.getEmployee(request.getEmail().trim());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("employee not found: " + request.getEmail().trim());
            }
            return existing.get();
        }
        throw new IllegalArgumentException("email is required");
    }

    private UUID resolveBranchId(String raw, UUID fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        String trimmed = raw.trim();
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return UUID.fromString(trimmed);
    }

    private EmployeeRole resolveRole(String raw, EmployeeRole fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        return EmployeeRole.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private String resolveString(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

}
