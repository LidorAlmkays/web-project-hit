package frontend.cli.adminmanagement;

import frontend.cli.shared.EmployeeManagementBaseController;
import frontend.cli.shared.ValidationUtils;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.admin.PasswordSettingsDto;
import shareddto.admin.PasswordSettingsUpdateRequest;
import shareddto.employeemanagement.request.BranchEmployeesRequest;
import shareddto.employeemanagement.request.EmployeeCreateRequest;
import shareddto.employeemanagement.request.EmployeeDeleteRequest;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import shareddto.employeemanagement.request.EmployeeUpdateRequest;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.Scanner;

public class AdminManagementController extends EmployeeManagementBaseController<AdminManagementView> {

    public AdminManagementController(IClientTransport client, AdminManagementView view, Scanner scanner) {
        super(client, view, scanner);
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
        String employeeId = view.prompt(scanner, "Employee ID");
        if (!ValidationUtils.isValidIsraeliId(employeeId)) {
            view.error("Employee ID must be a valid Israeli ID number.");
            return;
        }
        request.setEmployeeId(employeeId);
        String phoneNumber = view.prompt(scanner, "Phone number");
        if (!ValidationUtils.isValidPhoneDigits(phoneNumber)) {
            view.error("Phone number must contain only digits (9-10 digits).");
            return;
        }
        request.setPhoneNumber(phoneNumber);
        request.setBankAccountNumber(view.prompt(scanner, "Bank account number"));
        String email = view.prompt(scanner, "Email");
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
            return;
        }
        request.setEmail(email);
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
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
            return;
        }
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
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
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
        String email = view.prompt(scanner, "Email");
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
            return;
        }
        EmployeeGetRequest request = new EmployeeGetRequest(email);
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
                        if (!ValidationUtils.isValidIsraeliId(employeeId)) {
                            view.error("Employee ID must be a valid Israeli ID number.");
                            break;
                        }
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
                        if (!ValidationUtils.isValidPhoneDigits(phoneNumber)) {
                            view.error("Phone number must contain only digits (9-10 digits).");
                            break;
                        }
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

    private PasswordSettingsDto parsePasswordSettings(SocketMessage response) {
        return gson.fromJson(gson.toJsonTree(response.getData()), PasswordSettingsDto.class);
    }
}
