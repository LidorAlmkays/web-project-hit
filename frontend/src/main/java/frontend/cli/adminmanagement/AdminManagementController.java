package frontend.cli.adminmanagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.admin.PasswordSettingsDto;
import shareddto.admin.PasswordSettingsUpdateRequest;
import shareddto.employeemanagement.BranchCatalog;
import shareddto.employeemanagement.request.BranchEmployeesRequest;
import shareddto.employeemanagement.request.EmployeeCreateRequest;
import shareddto.employeemanagement.request.EmployeeDeleteRequest;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import shareddto.employeemanagement.request.EmployeeUpdateRequest;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminManagementController {
    private static final Gson gson = new Gson();
    private final IClientTransport client;
    private final AdminManagementView view;
    private final Scanner scanner;
    private final List<Map.Entry<String, String>> branchOptions;

    public AdminManagementController(IClientTransport client, AdminManagementView view, Scanner scanner) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
        this.branchOptions = new ArrayList<>(BranchCatalog.KNOWN_BRANCHES.entrySet());
    }

    public void run() throws IOException {
        view.header("Admin Management");
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
                    managePasswordPolicy();
                    break;
                case "7":
                    return;
                default:
                    view.error("Unknown option.");
                    break;
            }
        }
    }

    private void createEmployee() throws IOException {
        view.section("Create Employee");
        String role = view.promptRole(scanner);
        if (role.isEmpty()) {
            view.error("Role is required.");
            return;
        }
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        String branchId = promptBranchId("Branch", isAdmin, isAdmin ? "leave blank for admin" : "required");
        if (!isAdmin && branchId.isEmpty()) {
            view.error("Branch is required for non-admin employees.");
            return;
        }
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setRole(role);
        request.setBranchId(branchId.isEmpty() ? null : branchId);
        request.setFullName(view.prompt(scanner, "Full name"));
        request.setEmployeeId(view.prompt(scanner, "Employee ID"));
        request.setPhoneNumber(view.prompt(scanner, "Phone number"));
        request.setBankAccountNumber(view.prompt(scanner, "Bank account number"));
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

        SocketMessage response = sendOrReport(EventType.UPDATE_EMPLOYEE, request, "Update failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee updated.");
        view.printEmployee(toDisplayEmployee(parseEmployee(response)));
    }

    private void deleteEmployee() throws IOException {
        view.section("Delete Employee");
        String email = view.prompt(scanner, "Email");
        if (email == null || email.trim().isEmpty()) {
            view.error("Email is required.");
            return;
        }
        EmployeeDeleteRequest request = new EmployeeDeleteRequest(email.trim());
        SocketMessage response = sendOrReport(EventType.DELETE_EMPLOYEE, request, "Delete failed: ");
        if (response == null) {
            return;
        }
        view.success("Employee deleted.");
    }

    private void getEmployee() throws IOException {
        view.section("Get Employee");
        EmployeeGetRequest request = new EmployeeGetRequest(view.prompt(scanner, "Email"));
        SocketMessage response = sendOrReport(EventType.GET_EMPLOYEE, request, "Get failed: ");
        if (response == null) {
            return;
        }
        view.printEmployee(toDisplayEmployee(parseEmployee(response)));
    }

    private void listBranchEmployees() throws IOException {
        view.section("List Employees");
        BranchEmployeesRequest request = new BranchEmployeesRequest(promptBranchId("Branch", true, "leave blank for all"));
        SocketMessage response = sendOrReport(EventType.LIST_BRANCH_EMPLOYEES, request, "List failed: ");
        if (response == null) {
            return;
        }
        view.printEmployeeList(toDisplayEmployeeList(parseEmployeeList(response)));
    }

    private void managePasswordPolicy() throws IOException {
        view.section("Password Policy");
        SocketMessage response = sendOrReport(EventType.GET_PASSWORD_SETTINGS, null, "Fetch failed: ");
        if (response == null) {
            return;
        }
        PasswordSettingsDto current = parsePasswordSettings(response);
        view.info("Current password settings:");
        view.printPasswordSettings(current);

        boolean update = view.promptBoolean(scanner, "Update policy now", false);
        if (!update) {
            return;
        }
        boolean requireLength = view.promptBoolean(scanner, "Require at least 8 characters",
                current != null && current.isPasswordlength8());
        boolean requireUpper = view.promptBoolean(scanner, "Require at least one uppercase letter",
                current != null && current.isOneUpperletter());
        boolean requireNumber = view.promptBoolean(scanner, "Require at least one number",
                current != null && current.isOneNumber());

        PasswordSettingsUpdateRequest updateRequest = new PasswordSettingsUpdateRequest(
                requireLength, requireUpper, requireNumber);
        SocketMessage updatedResponse = sendOrReport(EventType.UPDATE_PASSWORD_SETTINGS, updateRequest,
                "Update failed: ");
        if (updatedResponse == null) {
            return;
        }
        view.success("Password policy updated.");
        view.printPasswordSettings(parsePasswordSettings(updatedResponse));
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
        return gson.fromJson(gson.toJsonTree(response.getData()), EmployeeDto.class);
    }

    private List<EmployeeDto> parseEmployeeList(SocketMessage response) {
        Type listType = new TypeToken<List<EmployeeDto>>() {
        }.getType();
        return gson.fromJson(gson.toJsonTree(response.getData()), listType);
    }

    private PasswordSettingsDto parsePasswordSettings(SocketMessage response) {
        return gson.fromJson(gson.toJsonTree(response.getData()), PasswordSettingsDto.class);
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
