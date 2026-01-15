package frontend.cli.chat;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import shareddto.chat.PendingRequestInfo;
import java.util.List;
import java.util.Scanner;

public class ChatRequestsCli implements IOptionCli {

    @Override
    public String getOptionName() {
        return "Check Chat Requests";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        EmployeeDto currentEmployee = SessionManager.getInstance().getCurrentEmployee();
        if (currentEmployee == null) {
            System.out.println("Error: Not logged in.");
            return CliResult.BACK;
        }

        String myEmail = currentEmployee.getEmail();

        System.out.println("\n=== Chat Requests ===");

        ChatPacket requestPacket = new ChatPacket(null, null, myEmail);
        SocketMessage response = client.send(EventType.GET_PENDING_REQUESTS, requestPacket);

        List<PendingRequestInfo> requests = null;
        if (response != null && response.getData() != null) {
            ChatPacket responsePacket = ChatModeHelper.extractChatPacket(response);
            if (responsePacket != null && responsePacket.getMessage() != null) {
                String rawMsg = responsePacket.getMessage();
                if (rawMsg.trim().startsWith("[")) {
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<PendingRequestInfo>>() {
                        }.getType();
                        requests = gson.fromJson(rawMsg, listType);
                    } catch (Exception e) {
                        System.out.println("Error parsing requests: " + e.getMessage());
                    }
                } else {
                    // Legacy fallback
                    requests = new java.util.ArrayList<>();
                    requests.add(new PendingRequestInfo(rawMsg, System.currentTimeMillis()));
                }
            }
        }

        if (requests == null || requests.isEmpty()) {
            System.out.println("No pending chat requests.");
            System.out.println("Tip: Someone needs to start a chat with your branch first.");
            return CliResult.BACK;
        }

        // Show numbered list
        System.out.println("\nPending Chat Requests:");
        for (int i = 0; i < requests.size(); i++) {
            PendingRequestInfo req = requests.get(i);
            long waitTimeMs = System.currentTimeMillis() - req.getTimestamp();
            long seconds = (waitTimeMs / 1000) % 60;
            long minutes = (waitTimeMs / (1000 * 60));
            System.out.printf("%d. %s (%dm %ds)\n", (i + 1), req.getRequesterEmail(), minutes, seconds);
        }
        System.out.println("0. Back");
        System.out.print("Choose request: ");

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

        if (choice < 1 || choice > requests.size()) {
            System.out.println("Invalid selection.");
            return CliResult.BACK;
        }

        PendingRequestInfo selectedRequest = requests.get(choice - 1);
        return handleRequestSelection(client, scanner, myEmail, selectedRequest);
    }

    private CliResult handleRequestSelection(IClientTransport client, Scanner scanner, String myEmail,
            PendingRequestInfo request) throws IOException {
        System.out.println("\nRequest from " + request.getRequesterEmail());
        System.out.println("What would you like to do?");
        System.out.println("1. Accept");
        System.out.println("2. Decline");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");

        String input = scanner.nextLine().trim();
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return CliResult.BACK;
        }

        switch (choice) {
            case 1:
                return acceptChat(client, scanner, myEmail, request.getRequesterEmail());
            case 2:
                declineChat(client, myEmail, request.getRequesterEmail());
                return CliResult.BACK;
            case 0:
            default:
                return CliResult.BACK;
        }
    }

    private CliResult acceptChat(IClientTransport client, Scanner scanner, String myEmail, String targetRequesterEmail)
            throws IOException {
        ChatPacket acceptPacket = new ChatPacket(null, null, myEmail + ":" + targetRequesterEmail);
        client.sendOnly(EventType.CHAT_ACCEPT, acceptPacket);

        System.out.println("Accepted! Entering chat mode...\n");
        ChatModeHelper.enterChatMode(client, scanner, myEmail);
        return CliResult.BACK;
    }

    private void declineChat(IClientTransport client, String myEmail, String targetRequesterEmail) throws IOException {
        ChatPacket declinePacket = new ChatPacket(null, null, myEmail + ":" + targetRequesterEmail);
        client.sendOnly(EventType.CHAT_DECLINE, declinePacket);
        System.out.println("Request declined.");
    }
}
