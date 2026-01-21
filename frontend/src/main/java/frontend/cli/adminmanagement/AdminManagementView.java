package frontend.cli.adminmanagement;

import frontend.cli.shared.BaseManagementView;
import shareddto.admin.PasswordSettingsDto;

import java.util.Locale;
import java.util.Scanner;

public class AdminManagementView extends BaseManagementView {

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

    public String promptRole(Scanner scanner) {
        return promptRole(scanner, true);
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

    public void printPasswordSettings(PasswordSettingsDto settings) {
        if (settings == null) {
            System.out.println("No data.");
            return;
        }
        System.out.println(toJson(settings));
    }
}
