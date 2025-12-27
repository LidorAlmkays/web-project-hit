package frontend.cli.employeemanagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import frontend.transport.IClientTransport;
import frontend.cli.employeemanagement.config.EmployeeManagementEvents;
import frontend.dto.employeemanagement.request.BranchEmployeesRequest;
import frontend.dto.employeemanagement.request.EmployeeCreateRequest;
import frontend.dto.employeemanagement.request.EmployeeDeleteRequest;
import frontend.dto.employeemanagement.request.EmployeeGetRequest;
import frontend.dto.employeemanagement.request.EmployeeUpdateRequest;
import frontend.dto.employeemanagement.response.EmployeeDto;
import shareddto.SocketMessage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;
import shareddto.EventType;

/**
 * Coordinates user input, API calls, and view rendering for employee management tasks.
 */
public class EmployeeManagementController {
    private static final Gson gson = new Gson();
    private final IClientTransport client;
    private final EmployeeManagementView view;
    private final Scanner scanner;

    public EmployeeManagementController(IClientTransport client, EmployeeManagementView view, Scanner scanner) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
    }

    /**
     * Runs the main CLI loop until the user exits.
     */
    public void run() throws IOException {
        view.header("Task 8 - Employee Management");
        while (true) {
            view.menu();
            if (!scanner.hasNextLine()) {
                view.info("No input. Exiting.");
                return;
            }
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    createEmployee();
                    break;
                case "2":
                    updateEmployee();
                    break;
                case "3":
                    deleteEmployee();
                    break;
                case "4":
                    getEmployee();
                    break;
                case "5":
                    listBranchEmployees();
                    break;
                case "6":
                    return;
                default:
                    view.error("Unknown option.");
                    break;
            }
        }
    }

    private void createEmployee() throws IOException {
        view.section("Create Employee");
        EmployeeCreateRequest request = promptEmployeeCreateRequest();

        SocketMessage response = sendOrReport(EmployeeManagementEvents.CREATE_EMPLOYEE, request, "Create failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee created.");
        view.printEmployee(parseEmployee(response));
        view.info("Tip: Use the Employee Number (UUID) above for Get/Update/Delete.");
    }

    private void updateEmployee() throws IOException {
        view.section("Update Employee");
        // Backend expects UUID employee number; adjust if server supports other identifiers.
        String employeeNumber = view.prompt(scanner, "Employee number (UUID)");
        EmployeeCreateRequest base = promptEmployeeCreateRequest();
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                employeeNumber,
                base.getBranchId(),
                base.getFullName(),
                base.getEmployeeId(),
                base.getPhoneNumber(),
                base.getBankAccountNumber(),
                base.getRole(),
                base.getEmail(),
                base.getPassword());

        SocketMessage response = sendOrReport(EmployeeManagementEvents.UPDATE_EMPLOYEE, request, "Update failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee updated.");
        view.printEmployee(parseEmployee(response));
    }

    private void deleteEmployee() throws IOException {
        view.section("Delete Employee");
        // Backend expects UUID employee number; adjust if server supports other identifiers.
        EmployeeDeleteRequest request = new EmployeeDeleteRequest(view.prompt(scanner, "Employee number (UUID)"));
        SocketMessage response = sendOrReport(EmployeeManagementEvents.DELETE_EMPLOYEE, request, "Delete failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee deleted.");
    }

    private void getEmployee() throws IOException {
        view.section("Get Employee");
        // Backend expects UUID employee number; adjust if server supports other identifiers.
        EmployeeGetRequest request = new EmployeeGetRequest(view.prompt(scanner, "Employee number (UUID)"));
        SocketMessage response = sendOrReport(EmployeeManagementEvents.GET_EMPLOYEE, request, "Get failed: ");
        if (response == null) {
            return;
        }
        view.printEmployee(parseEmployee(response));
    }

    private void listBranchEmployees() throws IOException {
        view.section("List Branch Employees");
        BranchEmployeesRequest request = new BranchEmployeesRequest(view.prompt(scanner, "Branch ID (UUID)"));
        SocketMessage response = sendOrReport(EmployeeManagementEvents.LIST_BRANCH_EMPLOYEES, request, "List failed: ");
        if (response == null) {
            return;
        }
        view.printEmployeeList(parseEmployeeList(response));
    }

    private EmployeeCreateRequest promptEmployeeCreateRequest() {
        return new EmployeeCreateRequest(
                view.prompt(scanner, "Branch ID (blank for admin)"),
                view.prompt(scanner, "Full name"),
                view.prompt(scanner, "Employee ID"),
                view.prompt(scanner, "Phone number"),
                view.prompt(scanner, "Bank account number"),
                view.promptRole(scanner),
                view.prompt(scanner, "Email"),
                view.prompt(scanner, "Password"));
    }

    /**
     * Sends a request and renders a user-friendly error on failure.
     */
    private SocketMessage sendOrReport(EventType event, Object request, String errorPrefix) throws IOException {
        SocketMessage response = client.send(event, request);
        if (response == null) {
            view.error(errorPrefix + "No response from server.");
            return null;
        }
        Object data = response.getData();
        if (data instanceof String) {
            view.error(errorPrefix + data);
            return null;
        }
        return response;
    }

    private EmployeeDto parseEmployee(SocketMessage response) {
        // SocketMessage data is untyped; Gson maps it into DTOs.
        return gson.fromJson(gson.toJsonTree(response.getData()), EmployeeDto.class);
    }

    private List<EmployeeDto> parseEmployeeList(SocketMessage response) {
        // List mapping mirrors server list payloads.
        Type listType = new TypeToken<List<EmployeeDto>>() {
        }.getType();
        return gson.fromJson(gson.toJsonTree(response.getData()), listType);
    }
}
