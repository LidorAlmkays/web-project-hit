package frontend.cli.employeemanagement;

import frontend.cli.shared.EmployeeManagementBaseController;
import frontend.cli.shared.ValidationUtils;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.employeemanagement.request.BranchEmployeesRequest;
import shareddto.employeemanagement.request.EmployeeCreateRequest;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import shareddto.employeemanagement.request.EmployeeUpdateRequest;
import shareddto.employeemanagement.response.EmployeeDto;
import shareddto.SocketMessage;

import java.io.IOException;
import java.util.Scanner;
import shareddto.EventType;


public class EmployeeManagementController extends EmployeeManagementBaseController<EmployeeManagementView> {

    public EmployeeManagementController(IClientTransport client, EmployeeManagementView view, Scanner scanner) {
        super(client, view, scanner);
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
        String role = view.promptRoleNonAdmin(scanner);
        if ("ADMIN".equalsIgnoreCase(role)) {
            view.error("Admin employees can only be created from Admin Management.");
            return;
        }
        request.setRole(role);
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
        EmployeeDto currentEmployee = getCurrentEmployeeWithBranch();
        if (currentEmployee == null) {
            return;
        }
        String email = view.prompt(scanner, "Email");
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
            return;
        }
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
                case "3":
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
}
