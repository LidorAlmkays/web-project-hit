package frontend.cli.shared;

import com.google.gson.reflect.TypeToken;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.employeemanagement.BranchCatalog;
import shareddto.employeemanagement.request.EmployeeGetRequest;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public abstract class EmployeeManagementBaseController<V extends BaseManagementView>
        extends BaseManagementController<V> {
    protected final List<Map.Entry<String, String>> branchOptions;

    protected EmployeeManagementBaseController(IClientTransport client, V view, Scanner scanner) {
        super(client, view, scanner);
        this.branchOptions = new ArrayList<>(BranchCatalog.KNOWN_BRANCHES.entrySet());
    }

    protected EmployeeDto fetchEmployeeByEmail(String email) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            view.error("Email is required to update an employee.");
            return null;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
            return null;
        }
        EmployeeGetRequest getRequest = new EmployeeGetRequest(email.trim());
        SocketMessage response = sendOrReport(EventType.GET_EMPLOYEE, getRequest, "Get failed: ");
        if (response == null) {
            return null;
        }
        return parseEmployee(response);
    }

    protected String promptBranchId(String label, boolean allowBlank, String blankHint) {
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

    protected Integer parseSelection(String input, int max) {
        try {
            int value = Integer.parseInt(input);
            if (value >= 1 && value <= max) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    protected EmployeeDto parseEmployee(SocketMessage response) {
        return gson.fromJson(gson.toJsonTree(response.getData()), EmployeeDto.class);
    }

    protected List<EmployeeDto> parseEmployeeList(SocketMessage response) {
        Type listType = new TypeToken<List<EmployeeDto>>() {
        }.getType();
        return gson.fromJson(gson.toJsonTree(response.getData()), listType);
    }

    protected EmployeeDto toDisplayEmployee(EmployeeDto employee) {
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

    protected List<EmployeeDto> toDisplayEmployeeList(List<EmployeeDto> employees) {
        if (employees == null || employees.isEmpty()) {
            return employees;
        }
        List<EmployeeDto> display = new ArrayList<>(employees.size());
        for (EmployeeDto employee : employees) {
            display.add(toDisplayEmployee(employee));
        }
        return display;
    }

    protected String getBranchDisplay(String branchId) {
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
