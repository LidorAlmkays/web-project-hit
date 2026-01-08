package frontend.cli.home;

import java.util.List;

public class HomeView {
    public void menu(List<String> options) {
        System.out.println("=== Home ===");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.println((options.size() + 1) + ". Logout");
        System.out.println((options.size() + 2) + ". Exit");
        System.out.print("Select an option: ");
    }

    public void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public void error(String message) {
        System.err.println("[ERROR] " + message);
    }
}
