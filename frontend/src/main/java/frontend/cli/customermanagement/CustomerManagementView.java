package frontend.cli.customermanagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import shareddto.customermanagement.response.CustomerDto;

import java.util.Scanner;

public class CustomerManagementView {
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
        System.out.println("1. Add customer");
        System.out.println("2. Get customer (by ID number)");
        System.out.println("3. Exit");
        System.out.print("Choose: ");
    }

    public String prompt(Scanner scanner, String label) {
        System.out.print(label + ": ");
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
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

    public void printCustomer(CustomerDto customer) {
        if (customer == null) {
            System.out.println("No data.");
            return;
        }
        System.out.println(gson.toJson(customer));
    }


    private String repeat(String text, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(text);
        }
        return sb.toString();
    }
}
