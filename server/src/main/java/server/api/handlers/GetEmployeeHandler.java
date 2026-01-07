package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import server.application.adaptors.EmployeeService;
import server.domain.employee.Employee;

import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

public class GetEmployeeHandler extends AbstractSocketHandler {
    private final EmployeeService employeeService;
    private final EmployeeRepositoryReader employeeRepositoryReader;

    public GetEmployeeHandler(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.employeeRepositoryReader = new EmployeeRepositoryReader();
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
                    new SocketMessage(EventType.GET_EMPLOYEE, EmployeeResponse.from(employee.get())));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.GET_EMPLOYEE, e.getMessage()));
        }
    }

    private Optional<Employee> resolveEmployee(EmployeeGetRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        if (request.getEmployeeNumber() != null && !request.getEmployeeNumber().trim().isEmpty()) {
            UUID employeeNumber = UUID.fromString(request.getEmployeeNumber().trim());
            return employeeService.getEmployee(employeeNumber);
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            return employeeRepositoryReader.findByEmail(request.getEmail().trim());
        }
        return Optional.empty();
    }

    private static class EmployeeGetRequest {
        private String employeeNumber;
        private String email;

        public String getEmployeeNumber() {
            return employeeNumber;
        }

        public String getEmail() {
            return email;
        }
    }
}
