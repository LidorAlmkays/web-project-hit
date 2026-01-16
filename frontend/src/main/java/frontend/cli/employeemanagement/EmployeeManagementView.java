package frontend.cli.employeemanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import shareddto.employeemanagement.response.EmployeeDto;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class EmployeeManagementView {
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
        System.out.println("1. Update employee (by email)");
        System.out.println("2. Get employee (by email)");
        System.out.println("3. List employees (all or by branch)");
        System.out.println("4. Exit");
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

    private String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }
}
