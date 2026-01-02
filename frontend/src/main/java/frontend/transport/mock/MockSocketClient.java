package frontend.transport.mock;

import frontend.dto.employeemanagement.request.BranchEmployeesRequest;
import frontend.dto.employeemanagement.request.EmployeeCreateRequest;
import frontend.dto.employeemanagement.request.EmployeeDeleteRequest;
import frontend.dto.employeemanagement.request.EmployeeGetRequest;
import frontend.dto.employeemanagement.request.EmployeeUpdateRequest;
import frontend.dto.employeemanagement.response.EmployeeDto;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory mock transport for local CLI testing without a server.
 */
public class MockSocketClient implements IClientTransport {
    private final Map<String, EmployeeDto> employees = new HashMap<>();
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
