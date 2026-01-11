package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.request.BranchEmployeesRequest;
import server.domain.employee.Employee;

import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class ListBranchEmployeesHandler extends AbstractSocketHandler {
    private final EmployeeRepositoryReader employeeRepositoryReader;

    public ListBranchEmployeesHandler() {
        this.employeeRepositoryReader = new EmployeeRepositoryReader();
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            BranchEmployeesRequest request = gson.fromJson(gson.toJsonTree(data), BranchEmployeesRequest.class);
            List<Employee> employees = resolveEmployees(request);
            sendMessage(clientSocket,
                    new SocketMessage(EventType.LIST_BRANCH_EMPLOYEES, EmployeeMapper.toDtoList(employees)));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.LIST_BRANCH_EMPLOYEES, e.getMessage()));
        }
    }

    private List<Employee> resolveEmployees(BranchEmployeesRequest request) {
        if (request == null || request.getBranchId() == null || request.getBranchId().trim().isEmpty()
                || "null".equalsIgnoreCase(request.getBranchId().trim())) {
            return employeeRepositoryReader.listAll();
        }
        UUID branchId = UUID.fromString(request.getBranchId().trim());
        return employeeRepositoryReader.findByBranchId(branchId);
    }
}
