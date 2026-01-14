package frontend.cli.chat;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.EventType;
import shareddto.chat.ChatPacket;
import shareddto.employeemanagement.BranchCatalog;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * CLI for starting a chat with another branch.
 */
public class StartBranchChatCli implements IOptionCli {

    @Override
    public String getOptionName() {
        return "Start Branch Chat";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        EmployeeDto currentEmployee = SessionManager.getInstance().getCurrentEmployee();
        if (currentEmployee == null) {
            System.out.println("Error: Not logged in.");
            return CliResult.BACK;
        }

        String myBranchId = currentEmployee.getBranchId();
        String myEmail = currentEmployee.getEmail();

        // Display available branches (exclude own branch)
        System.out.println("\n=== Start Branch Chat ===");
        System.out.println("Select a branch to chat with:");

        List<Map.Entry<String, String>> branches = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, String> entry : BranchCatalog.KNOWN_BRANCHES.entrySet()) {
            if (!entry.getKey().equals(myBranchId)) {
                System.out.println(index + ". " + entry.getValue());
                branches.add(entry);
                index++;
            }
        }
        System.out.println("0. Cancel");

        if (branches.isEmpty()) {
            System.out.println("No other branches available.");
            return CliResult.BACK;
        }

        // Get user selection
        System.out.print("Enter choice: ");
        String input = scanner.nextLine().trim();
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return CliResult.BACK;
        }

        if (choice == 0) {
            return CliResult.BACK;
        }

        if (choice < 1 || choice > branches.size()) {
            System.out.println("Invalid choice.");
            return CliResult.BACK;
        }

        String targetBranchId = branches.get(choice - 1).getKey();
        String targetBranchName = branches.get(choice - 1).getValue();

        System.out.println("Requesting chat with " + targetBranchName + "...");

        // Send chat request to server
        ChatPacket requestPacket = new ChatPacket(null, UUID.fromString(targetBranchId), myEmail);
        client.sendOnly(EventType.CHAT_REQUEST, requestPacket);

        System.out.println("Waiting for someone from " + targetBranchName + " to accept...");
        System.out.println("(You will receive a notification when connected)");

        // Enter chat mode using shared helper
        ChatModeHelper.enterChatMode(client, scanner, myEmail);

        return CliResult.BACK;
    }
}
