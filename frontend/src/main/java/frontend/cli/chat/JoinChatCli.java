package frontend.cli.chat;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ActiveChatInfo;
import shareddto.chat.ChatPacket;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class JoinChatCli implements IOptionCli {

    @Override
    public String getOptionName() {
        return "Join Active Chat";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        EmployeeDto currentEmployee = SessionManager.getInstance().getCurrentEmployee();
        if (currentEmployee == null) {
            System.out.println("Error: Not logged in.");
            return CliResult.BACK;
        }

        String myEmail = currentEmployee.getEmail();

        System.out.println("\n=== Active Chats ===");

        ChatPacket requestPacket = new ChatPacket(null, null, myEmail);
        SocketMessage response = client.send(EventType.GET_ACTIVE_CHATS, requestPacket);

        List<ActiveChatInfo> chats = null;
        if (response != null && response.getData() != null) {
            ChatPacket responsePacket = ChatModeHelper.extractChatPacket(response);
            if (responsePacket != null && responsePacket.getMessage() != null) {
                String rawMsg = responsePacket.getMessage();
                if (rawMsg.startsWith("ERROR:")) {
                    System.out.println(rawMsg.substring(6));
                    return CliResult.BACK;
                }
                if (rawMsg.trim().startsWith("[")) {
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<ActiveChatInfo>>() {
                        }.getType();
                        chats = gson.fromJson(rawMsg, listType);
                    } catch (Exception e) {
                        System.out.println("Error parsing chats: " + e.getMessage());
                    }
                }
            }
        }

        if (chats == null || chats.isEmpty()) {
            System.out.println("No active chats to join.");
            return CliResult.BACK;
        }

        System.out.println("\nActive Chat Sessions:");
        for (int i = 0; i < chats.size(); i++) {
            ActiveChatInfo info = chats.get(i);
            System.out.printf("%d. %s\n", (i + 1), info.getDisplayString());
        }
        System.out.println("0. Back");
        System.out.print("Choose session to join: ");

        String input = scanner.nextLine().trim();
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return CliResult.BACK;
        }

        if (choice == 0)
            return CliResult.BACK;

        if (choice < 1 || choice > chats.size()) {
            System.out.println("Invalid selection.");
            return CliResult.BACK;
        }

        ActiveChatInfo selectedChat = chats.get(choice - 1);
        return joinChat(client, scanner, myEmail, selectedChat.getSessionId());
    }

    private CliResult joinChat(IClientTransport client, Scanner scanner, String myEmail, String sessionId)
            throws IOException {
        ChatPacket joinPacket = new ChatPacket(null, null, myEmail + ":" + sessionId);
        client.sendOnly(EventType.MANAGER_JOIN, joinPacket);

        System.out.println("Joining chat...\n");
        ChatModeHelper.enterChatMode(client, scanner, myEmail, "Manager Chat");
        return CliResult.BACK;
    }
}
