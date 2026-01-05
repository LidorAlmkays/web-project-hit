package frontend.transport.mock;

import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.*;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.*;

/**
 * In-memory mock transport for local CLI testing without a server.
 */
public class MockSocketClient implements IClientTransport {
    private final Map<String, EmployeeDto> employees = new HashMap<>();
    // store plain-text passwords in the mock (for testing only)
    private final Map<String, String> passwords = new HashMap<>();
    // track logged-in users (optional for mock)
    private final Set<String> loggedIn = new HashSet<>();

    // seed a default admin user for convenience when running in mock/offline mode
    public MockSocketClient() {
        EmployeeDto admin = new EmployeeDto(
                "00000000-0000-0000-0000-000000000000",
                null,
                "Administrator",
                "admin",
                "000000000",
                "000000000",
                "ADMIN",
                "admin@local");
        employees.put("admin@local", admin);
        passwords.put("admin@local", "admin");
    }
    @Override
    public SocketMessage send(EventType eventType, Object data) throws IOException {
        if (eventType == null) {
            return error(EventType.LOGIN_EMPLOYEE, "Missing event type");
        }
        switch (eventType) {
            case CREATE_EMPLOYEE:
                return handleCreate(data);
            case UPDATE_EMPLOYEE:
                return handleUpdate(data);
            case DELETE_EMPLOYEE:
                return handleDelete(data);
            case GET_EMPLOYEE:
                return handleGet(data);
            case LIST_BRANCH_EMPLOYEES:
                return handleListBranch(data);
            case LOGIN_EMPLOYEE:
                return handleLogin(data);
            case LOGOUT_EMPLOYEE:
                return handleLogout(data);
            default:
                return error(eventType, "Unsupported event in mock: " + eventType);
        }
    }

    @Override
    public SocketMessage receive() throws IOException {
        return error(EventType.LOGIN_EMPLOYEE, "Mock transport does not support receive without send");
    }

    @Override
    public void close() throws IOException {
    }

    private SocketMessage handleCreate(Object data) {
        EmployeeCreateRequest request = (EmployeeCreateRequest) data;
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            return error(EventType.CREATE_EMPLOYEE, "Missing employee data");
        }
        String email = request.getEmail().trim();
        if (employees.containsKey(email)) {
            return error(EventType.CREATE_EMPLOYEE, "Email already exists");
        }

        EmployeeDto employee = new EmployeeDto(
                UUID.randomUUID().toString(),
                normalizeBlank(request.getBranchId()),
                request.getFullName(),
                request.getEmployeeId(),
                request.getPhoneNumber(),
                request.getBankAccountNumber(),
                request.getRole(),
                email);
        employees.put(email, employee);
        // Store password for mock authentication (testing only)
        passwords.put(email, request.getPassword());
        return ok(EventType.CREATE_EMPLOYEE, employee);
    }

    private SocketMessage handleUpdate(Object data) {
        EmployeeUpdateRequest request = (EmployeeUpdateRequest) data;
        if (request == null || isBlank(request.getEmployeeNumber()) || isBlank(request.getEmail())) {
            return error(EventType.UPDATE_EMPLOYEE, "Missing employee number or email");
        }

        EmployeeDto existing = findByEmployeeNumber(request.getEmployeeNumber().trim());
        if (existing == null) {
            return error(EventType.UPDATE_EMPLOYEE, "Employee not found");
        }

        String newEmail = request.getEmail().trim();
        if (!existing.getEmail().equals(newEmail) && employees.containsKey(newEmail)) {
            return error(EventType.UPDATE_EMPLOYEE, "Email already exists");
        }

        employees.remove(existing.getEmail());
        EmployeeDto updated = new EmployeeDto(
                existing.getEmployeeNumber(),
                normalizeBlank(request.getBranchId()),
                request.getFullName(),
                request.getEmployeeId(),
                request.getPhoneNumber(),
                request.getBankAccountNumber(),
                request.getRole(),
                newEmail);
        employees.put(newEmail, updated);
        return ok(EventType.UPDATE_EMPLOYEE, updated);
    }

    private SocketMessage handleDelete(Object data) {
        EmployeeDeleteRequest request = (EmployeeDeleteRequest) data;
        if (request == null || isBlank(request.getEmployeeNumber())) {
            return error(EventType.DELETE_EMPLOYEE, "Missing employee number");
        }

        EmployeeDto existing = findByEmployeeNumber(request.getEmployeeNumber().trim());
        if (existing == null) {
            return error(EventType.DELETE_EMPLOYEE, "Employee not found");
        }

        employees.remove(existing.getEmail());
        return ok(EventType.DELETE_EMPLOYEE, null);
    }

    private SocketMessage handleGet(Object data) {
        EmployeeGetRequest request = (EmployeeGetRequest) data;
        if (request == null || isBlank(request.getEmployeeNumber())) {
            return error(EventType.GET_EMPLOYEE, "Missing employee number");
        }

        EmployeeDto existing = findByEmployeeNumber(request.getEmployeeNumber().trim());
        if (existing == null) {
            return error(EventType.GET_EMPLOYEE, "Employee not found");
        }
        return ok(EventType.GET_EMPLOYEE, existing);
    }

    private SocketMessage handleListBranch(Object data) {
        BranchEmployeesRequest request = (BranchEmployeesRequest) data;
        if (request == null || isBlank(request.getBranchId())) {
            return error(EventType.LIST_BRANCH_EMPLOYEES, "Missing branch id");
        }
        String branchId = request.getBranchId().trim();
        List<EmployeeDto> list = new ArrayList<>();
        for (EmployeeDto employee : employees.values()) {
            if (branchId.equals(employee.getBranchId())) {
                list.add(employee);
            }
        }
        return ok(EventType.LIST_BRANCH_EMPLOYEES, list);
    }

    private SocketMessage handleLogout(Object data) {
        // Parse typed DTO first; otherwise convert untyped data into DTO using Gson
        String employeeNumber = null;
        if (data instanceof shareddto.employeemanagement.request.LogoutEmployeeRequest) {
            employeeNumber = ((shareddto.employeemanagement.request.LogoutEmployeeRequest) data).getEmployeeNumber();
        } else {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            try {
                shareddto.employeemanagement.request.LogoutEmployeeRequest req =
                        gson.fromJson(gson.toJson(data), shareddto.employeemanagement.request.LogoutEmployeeRequest.class);
                if (req != null) {
                    employeeNumber = req.getEmployeeNumber();
                }
            } catch (Exception ignored) {
                // fall through to error handling below
            }
        }

        if (isBlank(employeeNumber)) {
            return error(EventType.LOGOUT_EMPLOYEE, "Missing employee number");
        }

        EmployeeDto existing = findByEmployeeNumber(employeeNumber.trim());
        if (existing == null) {
            return error(EventType.LOGOUT_EMPLOYEE, "Employee not found");
        }

        // Remove from logged-in set (mock behavior)
        loggedIn.remove(existing.getEmail());
        return ok(EventType.LOGOUT_EMPLOYEE, null);
    }

    private SocketMessage handleLogin(Object data) {
        LoginEmployeeRequest request = (LoginEmployeeRequest) data;
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            return error(EventType.LOGIN_EMPLOYEE, "Missing email or password");
        }

        String email = request.getEmail().trim();
        String password = request.getPassword().trim();

        EmployeeDto existing = employees.get(email);
        if (existing == null) {
            return error(EventType.LOGIN_EMPLOYEE, "User not found");
        }

        String expected = passwords.get(email);
        if (expected == null || !expected.equals(password)) {
            return error(EventType.LOGIN_EMPLOYEE, "Invalid credentials");
        }

        // mark as logged in in mock
        loggedIn.add(email);

        return ok(EventType.LOGIN_EMPLOYEE, existing);
    }

    private EmployeeDto findByEmployeeNumber(String employeeNumber) {
        if (isBlank(employeeNumber)) {
            return null;
        }
        String normalized = employeeNumber.trim();
        for (EmployeeDto employee : employees.values()) {
            if (normalized.equals(employee.getEmployeeNumber())) {
                return employee;
            }
        }
        return null;
    }

    private SocketMessage ok(EventType eventType, Object data) {
        return new SocketMessage(eventType, data);
    }

    private SocketMessage error(EventType eventType, String message) {
        return new SocketMessage(eventType, message);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeBlank(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
