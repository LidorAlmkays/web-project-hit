package frontend.cli.employeemanagement;

import frontend.cli.shared.BaseManagementView;

import java.util.Scanner;


public class EmployeeManagementView extends BaseManagementView {

    public void menu() {
        section("Main Menu");
        System.out.println("1. Create employee");
        System.out.println("2. Update employee (by email)");
        System.out.println("3. Get employee (by email)");
        System.out.println("4. List employees (current branch)");
        System.out.println("5. Exit");
        System.out.print("Choose: ");
    }

    public String promptRoleNonAdmin(Scanner scanner) {
        return promptRole(scanner, false);
    }
}
