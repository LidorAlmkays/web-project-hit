package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import server.application.adaptors.EmployeeService;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;

import java.net.Socket;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class UpdateEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;
    private final EmployeeRepositoryReader employeeRepositoryReader;

    public UpdateEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.employeeRepositoryReader = new EmployeeRepositoryReader();
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
                    new SocketMessage(EventType.UPDATE_EMPLOYEE, EmployeeResponse.from(saved)));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.UPDATE_EMPLOYEE, e.getMessage()));
        }
    }

    private Employee resolveExisting(EmployeeUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getEmployeeNumber() != null && !request.getEmployeeNumber().trim().isEmpty()) {
            UUID employeeNumber = parseRequiredUuid(request.getEmployeeNumber(), "employeeNumber is required");
            Optional<Employee> existing = employeeService.getEmployee(employeeNumber);
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("employee not found: " + employeeNumber);
            }
            return existing.get();
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Optional<Employee> existing = employeeRepositoryReader.findByEmail(request.getEmail().trim());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("employee not found: " + request.getEmail().trim());
            }
            return existing.get();
        }
        throw new IllegalArgumentException("employeeNumber or email is required");
    }

    private UUID parseRequiredUuid(String raw, String errorMessage) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return UUID.fromString(raw.trim());
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

    private static class EmployeeUpdateRequest {
        private String employeeNumber;
        private String branchId;
        private String fullName;
        private String employeeId;
        private String phoneNumber;
        private String bankAccountNumber;
        private String role;
        private String email;
        private String password;

        public String getEmployeeNumber() {
            return employeeNumber;
        }

        public String getBranchId() {
            return branchId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getBankAccountNumber() {
            return bankAccountNumber;
        }

        public String getRole() {
            return role;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
