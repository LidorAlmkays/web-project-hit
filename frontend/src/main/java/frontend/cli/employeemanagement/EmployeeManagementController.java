package frontend.cli.employeemanagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.employeemanagement.request.BranchEmployeesRequest;
import shareddto.employeemanagement.request.EmployeeCreateRequest;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import shareddto.employeemanagement.request.EmployeeUpdateRequest;
import shareddto.employeemanagement.BranchCatalog;
import shareddto.employeemanagement.response.EmployeeDto;
import shareddto.SocketMessage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import shareddto.EventType;


public class EmployeeManagementController {
    private static final Gson gson = new Gson();
    private final IClientTransport client;
    private final EmployeeManagementView view;
    private final Scanner scanner;
    private final List<Map.Entry<String, String>> branchOptions;

    public EmployeeManagementController(IClientTransport client, EmployeeManagementView view, Scanner scanner) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
        this.branchOptions = new ArrayList<>(BranchCatalog.KNOWN_BRANCHES.entrySet());
    }


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
                    getEmployee();
                    break;
                case "4":
                    listBranchEmployees();
                    break;
                case "5":
                    return;
                default:
                    view.error("Unknown option.");
                    break;
            }
        }
    }

    private void createEmployee() throws IOException {
        view.section("Create Employee");
        EmployeeDto currentEmployee = getCurrentEmployeeWithBranch();
        if (currentEmployee == null) {
            return;
        }
        if (!isManager(currentEmployee.getRole())) {
            view.error("Only shift managers and admins can create employees.");
            return;
        }
        String branchId = currentEmployee.getBranchId();
        if (branchId == null || branchId.trim().isEmpty()) {
            view.error("No branch assigned to the current user.");
            return;
        }
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setBranchId(branchId);
        request.setFullName(view.prompt(scanner, "Full name"));
        request.setEmployeeId(view.prompt(scanner, "Employee ID"));
        request.setPhoneNumber(view.prompt(scanner, "Phone number"));
        request.setBankAccountNumber(view.prompt(scanner, "Bank account number"));
        String role = view.promptRoleNonAdmin(scanner);
        if ("ADMIN".equalsIgnoreCase(role)) {
            view.error("Admin employees can only be created from Admin Management.");
            return;
        }
        request.setRole(role);
        request.setEmail(view.prompt(scanner, "Email"));
        request.setPassword(view.prompt(scanner, "Password"));

        SocketMessage response = sendOrReport(EventType.CREATE_EMPLOYEE, request, "Create failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee created.");
        view.printEmployee(toDisplayEmployee(parseEmployee(response)));
    }

    private void updateEmployee() throws IOException {
        view.section("Update Employee");
        EmployeeDto currentEmployee = getCurrentEmployeeWithBranch();
        if (currentEmployee == null) {
            return;
        }
        String email = view.prompt(scanner, "Email");
        EmployeeDto current = fetchEmployeeByEmail(email);
        if (current == null) {
            return;
        }
        if (!isSameBranch(currentEmployee, current)) {
            view.error("You can only update employees in your branch.");
            return;
        }
        view.info("Current employee data:");
        view.printEmployee(toDisplayEmployee(current));
        EmployeeUpdateRequest request = promptEmployeeUpdateRequest(email, current);
        if (request == null) {
            view.info("No changes selected.");
            return;
        }

        SocketMessage response = sendOrReport(EventType.UPDATE_EMPLOYEE, request, "Update failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee updated.");
        view.printEmployee(toDisplayEmployee(parseEmployee(response)));
    }

    private void getEmployee() throws IOException {
        view.section("Get Employee");
        EmployeeDto currentEmployee = getCurrentEmployeeWithBranch();
        if (currentEmployee == null) {
            return;
        }
        EmployeeGetRequest request = new EmployeeGetRequest(view.prompt(scanner, "Email"));
        SocketMessage response = sendOrReport(EventType.GET_EMPLOYEE, request, "Get failed: ");
        if (response == null) {
            return;
        }
        EmployeeDto employee = parseEmployee(response);
        if (!isSameBranch(currentEmployee, employee)) {
            view.error("You can only view employees in your branch.");
            return;
        }
        view.printEmployee(toDisplayEmployee(employee));
    }

    private void listBranchEmployees() throws IOException {
        view.section("List Employees");
        EmployeeDto currentEmployee = getCurrentEmployeeWithBranch();
        if (currentEmployee == null) {
            return;
        }
        BranchEmployeesRequest request = new BranchEmployeesRequest(currentEmployee.getBranchId());
        SocketMessage response = sendOrReport(EventType.LIST_BRANCH_EMPLOYEES, request, "List failed: ");
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
        SocketMessage response = sendOrReport(EventType.GET_EMPLOYEE, getRequest, "Get failed: ");
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
            view.info("1. Full name");
            view.info("2. Employee ID");
            view.info("3. Phone number");
            view.info("4. Bank account number");
            view.info("5. Role");
            view.info("6. Password");
            view.info("7. Done");
            String choice = view.prompt(scanner, "Choose");
            switch (choice) {
                case "1":
                    String fullName = view.prompt(scanner, "Full name");
                    if (!fullName.trim().isEmpty()) {
                        request.setFullName(fullName);
                        draft.setFullName(fullName);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "2":
                    String employeeId = view.prompt(scanner, "Employee ID");
                    if (!employeeId.trim().isEmpty()) {
                        request.setEmployeeId(employeeId);
                        draft.setEmployeeId(employeeId);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "3":
                    String phoneNumber = view.prompt(scanner, "Phone number");
                    if (!phoneNumber.trim().isEmpty()) {
                        request.setPhoneNumber(phoneNumber);
                        draft.setPhoneNumber(phoneNumber);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "4":
                    String bankAccountNumber = view.prompt(scanner, "Bank account number");
                    if (!bankAccountNumber.trim().isEmpty()) {
                        request.setBankAccountNumber(bankAccountNumber);
                        draft.setBankAccountNumber(bankAccountNumber);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "5":
                    String role = view.promptRoleNonAdmin(scanner);
                    if (!role.trim().isEmpty()) {
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            view.error("Admin role changes are only allowed in Admin Management.");
                            break;
                        }
                        request.setRole(role);
                        draft.setRole(role);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "6":
                    String password = view.prompt(scanner, "Password");
                    if (!password.trim().isEmpty()) {
                        request.setPassword(password);
                        hasChanges = true;
                    }
                    view.info("Current employee data:");
                    view.printEmployee(toDisplayEmployee(draft));
                    break;
                case "7":
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
            Map.Entry<String, String> option = branchOptions.get(i);
            view.info(String.format("  %d) %s", i + 1, option.getValue()));
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
                return branchOptions.get(selection - 1).getKey();
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

    private EmployeeDto getCurrentEmployeeWithBranch() {
        EmployeeDto currentEmployee = SessionManager.getInstance().getCurrentEmployee();
        if (currentEmployee == null) {
            view.error("No logged-in user information available.");
            return null;
        }
        if (currentEmployee.getBranchId() == null || currentEmployee.getBranchId().trim().isEmpty()) {
            view.error("No branch assigned to the current user. Use Admin Management for cross-branch access.");
            return null;
        }
        return currentEmployee;
    }

    private boolean isManager(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "SHIFT_MANAGER".equalsIgnoreCase(role);
    }

    private boolean isSameBranch(EmployeeDto currentEmployee, EmployeeDto target) {
        if (currentEmployee == null || target == null) {
            return false;
        }
        String currentBranch = currentEmployee.getBranchId();
        String targetBranch = target.getBranchId();
        if (currentBranch == null || targetBranch == null) {
            return false;
        }
        return currentBranch.trim().equalsIgnoreCase(targetBranch.trim());
    }



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
        for (Map.Entry<String, String> option : branchOptions) {
            if (option.getKey().equals(branchId)) {
                return option.getValue();
            }
        }
        return branchId;
    }
}
