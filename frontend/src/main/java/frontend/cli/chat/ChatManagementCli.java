package frontend.cli.chat;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Submenu for chat management options.
 */
public class ChatManagementCli implements IOptionCli {

    private final List<IOptionCli> chatOptions = List.of(
            new StartBranchChatCli(),
            new ChatRequestsCli());

    @Override
    public String getOptionName() {
        return "Chats";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        while (true) {
            System.out.println("\n=== Chat Management ===");
            for (int i = 0; i < chatOptions.size(); i++) {
                System.out.println((i + 1) + ". " + chatOptions.get(i).getOptionName());
            }
            System.out.println("0. Back");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            if (choice == 0) {
                return CliResult.BACK;
            }

            if (choice < 1 || choice > chatOptions.size()) {
                System.out.println("Invalid option.");
                continue;
            }

            CliResult result = chatOptions.get(choice - 1).run(client, scanner);
            if (result == CliResult.EXIT) {
                return CliResult.EXIT;
            }
            // BACK or LOGOUT just returns to this submenu
        }
    }
}
