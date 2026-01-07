package frontend.cli.employeemanagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import frontend.transport.IClientTransport;
import frontend.cli.employeemanagement.config.EmployeeManagementEvents;
import shareddto.employeemanagement.request.BranchEmployeesRequest;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import shareddto.employeemanagement.request.EmployeeUpdateRequest;
import shareddto.employeemanagement.response.EmployeeDto;
import shareddto.SocketMessage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final List<BranchOption> branchOptions;

    public EmployeeManagementController(IClientTransport client, EmployeeManagementView view, Scanner scanner) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
        this.branchOptions = new ArrayList<>(KNOWN_BRANCHES);
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
                    updateEmployee();
                    break;
                case "2":
                    getEmployee();
                    break;
                case "3":
                    listBranchEmployees();
                    break;
                case "4":
                    return;
                default:
                    view.error("Unknown option.");
                    break;
            }
        }
    }

    private void updateEmployee() throws IOException {
        view.section("Update Employee");
        String email = view.prompt(scanner, "Email");
        EmployeeDto current = fetchEmployeeByEmail(email);
        if (current == null) {
            return;
        }
        view.info("Current employee data:");
        view.printEmployee(toDisplayEmployee(current));
        EmployeeUpdateRequest request = promptEmployeeUpdateRequest(email, current);
        if (request == null) {
            view.info("No changes selected.");
            return;
        }

        SocketMessage response = sendOrReport(EmployeeManagementEvents.UPDATE_EMPLOYEE, request, "Update failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee updated.");
        view.printEmployee(toDisplayEmployee(parseEmployee(response)));
    }

    private void getEmployee() throws IOException {
        view.section("Get Employee");
        EmployeeGetRequest request = new EmployeeGetRequest(view.prompt(scanner, "Email"));
        SocketMessage response = sendOrReport(EmployeeManagementEvents.GET_EMPLOYEE, request, "Get failed: ");
        if (response == null) {
            return;
        }
        view.printEmployee(toDisplayEmployee(parseEmployee(response)));
    }

    private void listBranchEmployees() throws IOException {
        view.section("List Employees");
        BranchEmployeesRequest request = new BranchEmployeesRequest(promptBranchId("Branch", true, "leave blank for all"));
        SocketMessage response = sendOrReport(EmployeeManagementEvents.LIST_BRANCH_EMPLOYEES, request, "List failed: ");
        if (response == null) {
            return;
        }
        view.printEmployeeList(toDisplayEmployeeList(parseEmployeeList(response)));
    }

    private EmployeeDto fetchEmployeeByEmail(String email) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            view.error("Email is required to update an employee.");
            return null;
        }
        EmployeeGetRequest getRequest = new EmployeeGetRequest(email.trim());
        SocketMessage response = sendOrReport(EmployeeManagementEvents.GET_EMPLOYEE, getRequest, "Get failed: ");
        if (response == null) {
            return null;
        }
        return parseEmployee(response);
    }

    private EmployeeUpdateRequest promptEmployeeUpdateRequest(String email, EmployeeDto current) {
        if (email == null || email.trim().isEmpty()) {
            view.error("Email is required to update an employee.");
            return null;
        }
        if (current == null) {
            view.error("Employee data is required to update.");
            return null;
        }
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setEmployeeNumber(current.getEmployeeNumber());
        request.setEmail(email.trim());
        boolean hasChanges = false;
        EmployeeDto draft = new EmployeeDto(
                current.getEmployeeNumber(),
                current.getBranchId(),
                current.getFullName(),
                current.getEmployeeId(),
                current.getPhoneNumber(),
                current.getBankAccountNumber(),
                current.getRole(),
                current.getEmail());

        while (true) {
            view.section("Select Fields to Edit");
            view.info("1. Branch");
            view.info("2. Full name");
            view.info("3. Employee ID");
            view.info("4. Phone number");
            view.info("5. Bank account number");
            view.info("6. Role");
            view.info("7. Password");
            view.info("8. Done");
            String choice = view.prompt(scanner, "Choose");
            switch (choice) {
                case "1":
                    String branchId = promptBranchId("Branch", true, "leave blank to keep current");
                    if (!branchId.isEmpty()) {
                        request.setBranchId(branchId);
                        draft.setBranchId(branchId);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "2":
                    String fullName = view.prompt(scanner, "Full name");
                    if (!fullName.trim().isEmpty()) {
                        request.setFullName(fullName);
                        draft.setFullName(fullName);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "3":
                    String employeeId = view.prompt(scanner, "Employee ID");
                    if (!employeeId.trim().isEmpty()) {
                        request.setEmployeeId(employeeId);
                        draft.setEmployeeId(employeeId);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "4":
                    String phoneNumber = view.prompt(scanner, "Phone number");
                    if (!phoneNumber.trim().isEmpty()) {
                        request.setPhoneNumber(phoneNumber);
                        draft.setPhoneNumber(phoneNumber);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "5":
                    String bankAccountNumber = view.prompt(scanner, "Bank account number");
                    if (!bankAccountNumber.trim().isEmpty()) {
                        request.setBankAccountNumber(bankAccountNumber);
                        draft.setBankAccountNumber(bankAccountNumber);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "6":
                    String role = view.promptRole(scanner);
                    if (!role.trim().isEmpty()) {
                        request.setRole(role);
                        draft.setRole(role);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "7":
                    String password = view.prompt(scanner, "Password");
                    if (!password.trim().isEmpty()) {
                        request.setPassword(password);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "8":
                    return hasChanges ? request : null;
                default:
                    view.error("Unknown option.");
                    break;
            }
        }
    }

    private String promptBranchId(String label, boolean allowBlank, String blankHint) {
        if (branchOptions.isEmpty()) {
            String suffix = allowBlank ? " (" + blankHint + ")" : "";
            return view.prompt(scanner, label + suffix);
        }

        view.info("Known branches:");
        for (int i = 0; i < branchOptions.size(); i++) {
            BranchOption option = branchOptions.get(i);
            view.info(String.format("  %d) %s", i + 1, option.name));
        }
        String hint = allowBlank
                ? "Choose a number from the list or " + blankHint
                : "Choose a number from the list";
        while (true) {
            String input = view.prompt(scanner, label + " - " + hint);
            if (input.isEmpty() && allowBlank) {
                return "";
            }
            Integer selection = parseSelection(input, branchOptions.size());
            if (selection != null) {
                return branchOptions.get(selection - 1).id;
            }
            view.error("Invalid selection. Enter a number from the list.");
        }
    }

    private Integer parseSelection(String input, int max) {
        try {
            int value = Integer.parseInt(input);
            if (value >= 1 && value <= max) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static final List<BranchOption> KNOWN_BRANCHES = Arrays.asList(
            new BranchOption("550e8400-e29b-41d4-a716-446655440001", "Downtown Premium Store"),
            new BranchOption("550e8400-e29b-41d4-a716-446655440002", "Mall Branch - High Traffic"),
            new BranchOption("550e8400-e29b-41d4-a716-446655440003", "Industrial Outlet")
    );

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

    private EmployeeDto toDisplayEmployee(EmployeeDto employee) {
        if (employee == null) {
            return null;
        }
        String displayBranch = getBranchDisplay(employee.getBranchId());
        if (displayBranch.equals(employee.getBranchId())) {
            return employee;
        }
        return new EmployeeDto(
                employee.getEmployeeNumber(),
                displayBranch,
                employee.getFullName(),
                employee.getEmployeeId(),
                employee.getPhoneNumber(),
                employee.getBankAccountNumber(),
                employee.getRole(),
                employee.getEmail());
    }

    private List<EmployeeDto> toDisplayEmployeeList(List<EmployeeDto> employees) {
        if (employees == null || employees.isEmpty()) {
            return employees;
        }
        List<EmployeeDto> display = new ArrayList<>(employees.size());
        for (EmployeeDto employee : employees) {
            display.add(toDisplayEmployee(employee));
        }
        return display;
    }

    private String getBranchDisplay(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            return branchId;
        }
        for (BranchOption option : branchOptions) {
            if (option.id.equals(branchId)) {
                return option.name;
            }
        }
        return branchId;
    }

    private static class BranchOption {
        private final String id;
        private final String name;

        private BranchOption(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
