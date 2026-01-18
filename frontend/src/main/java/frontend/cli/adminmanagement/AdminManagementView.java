package frontend.cli.adminmanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import shareddto.admin.PasswordSettingsDto;
import shareddto.employeemanagement.response.EmployeeDto;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminManagementView {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void header(String title) {
        System.out.println();
        System.out.println(title);
        System.out.println(repeat("=", title.length()));
    }

    public void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    public void menu() {
        section("Main Menu");
        System.out.println("1. Create employee");
        System.out.println("2. Update employee");
        System.out.println("3. Delete employee");
        System.out.println("4. Get employee");
        System.out.println("5. List employees");
        System.out.println("6. Password policy");
        System.out.println("7. Exit");
        System.out.print("Choose: ");
    }

    public String prompt(Scanner scanner, String label) {
        System.out.print(label + ": ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    public String promptRole(Scanner scanner) {
        System.out.print("Role (SHIFT_MANAGER, CASHIER, SELLER, ADMIN): ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        String role = scanner.nextLine().trim();
        return role.toUpperCase(Locale.ROOT);
    }

    public boolean promptBoolean(Scanner scanner, String label, boolean currentValue) {
        System.out.print(label + " (y/n, current: " + currentValue + "): ");
        if (!scanner.hasNextLine()) {
            return currentValue;
        }
        String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        if (input.isEmpty()) {
            return currentValue;
        }
        return input.startsWith("y");
    }

    public void success(String message) {
        System.out.println("OK: " + message);
    }

    public void error(String message) {
        System.out.println("ERROR: " + message);
    }

    public void info(String message) {
        System.out.println(message);
    }

    public void printEmployee(EmployeeDto employee) {
        if (employee == null) {
            System.out.println("No data.");
            return;
        }
        System.out.println(gson.toJson(employee));
    }

    public void printEmployeeList(List<EmployeeDto> employees) {
        if (employees == null || employees.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        System.out.println(gson.toJson(employees));
    }

    public void printPasswordSettings(PasswordSettingsDto settings) {
        if (settings == null) {
            System.out.println("No data.");
            return;
        }
        System.out.println(gson.toJson(settings));
    }

    private String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }
}
