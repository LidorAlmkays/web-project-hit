package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import server.application.adaptors.EmployeeService;
import server.domain.employee.Employee;

import java.net.Socket;
import java.util.Optional;

public class GetEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;

    public GetEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            EmployeeGetRequest request = gson.fromJson(gson.toJsonTree(data), EmployeeGetRequest.class);
            Optional<Employee> employee = resolveEmployee(request);
            if (employee.isEmpty()) {
                throw new IllegalArgumentException("employee not found");
            }
            sendMessage(clientSocket,
                    new SocketMessage(EventType.GET_EMPLOYEE, EmployeeMapper.toDto(employee.get())));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.GET_EMPLOYEE, e.getMessage()));
        }
    }

    private Optional<Employee> resolveEmployee(EmployeeGetRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            return employeeService.getEmployee(request.getEmail().trim());
        }
        return Optional.empty();
    }
}
