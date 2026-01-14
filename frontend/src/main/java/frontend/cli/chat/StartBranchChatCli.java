package frontend.cli.chat;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;
import frontend.util.SessionManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;
import shareddto.employeemanagement.BranchCatalog;
import shareddto.employeemanagement.response.EmployeeDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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

        // Enter chat mode with listener thread
        enterChatMode(client, scanner, myEmail);

        return CliResult.BACK;
    }

    private void enterChatMode(IClientTransport client, Scanner scanner, String myEmail) throws IOException {
        System.out.println("\n--- Chat Mode ---");
        System.out.println("Type your message and press Enter to send.");
        System.out.println("Type /exit to leave the chat.\n");

        // Flag to control the listener thread and main loop
        AtomicBoolean running = new AtomicBoolean(true);

        // Start background listener thread
        Thread listenerThread = new Thread(() -> {
            while (running.get()) {
                try {
                    SocketMessage message = client.receive();
                    if (message != null) {
                        handleIncomingMessage(message, running);
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        System.out.println("\n[Connection error: " + e.getMessage() + "]");
                        running.set(false);
                    }
                    break;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // Main input loop
        while (running.get()) {
            // Check if we're still running before blocking on input
            if (!running.get())
                break;

            String message = scanner.nextLine();

            if (!running.get())
                break; // Check again after input

            if (message.equalsIgnoreCase("/exit")) {
                // Send close request
                ChatPacket closePacket = new ChatPacket(null, null, myEmail);
                client.sendOnly(EventType.CHAT_CLOSE, closePacket);
                System.out.println("Chat closed.");
                running.set(false);
            } else if (!message.trim().isEmpty()) {
                // Send message
                ChatPacket msgPacket = new ChatPacket(null, null, myEmail + ":" + message);
                client.sendOnly(EventType.CHAT_MESSAGE, msgPacket);
            }
        }

        // Clean up listener thread
        running.set(false);
        listenerThread.interrupt();
    }

    private void handleIncomingMessage(SocketMessage message, AtomicBoolean running) {
        EventType eventType = message.getEventType();

        switch (eventType) {
            case CHAT_MESSAGE:
                // Display received message
                if (message.getData() != null) {
                    ChatPacket packet = extractChatPacket(message);
                    if (packet != null && packet.getMessage() != null) {
                        System.out.println(packet.getMessage());
                    }
                }
                break;

            case CHAT_REQUEST:
                // Notification about incoming request (for when you're the target)
                if (message.getData() != null) {
                    ChatPacket packet = extractChatPacket(message);
                    if (packet != null) {
                        System.out.println("\n[Chat Request: " + packet.getMessage() + "]");
                    }
                }
                break;

            case CHAT_CLOSE:
                System.out.println("\n[Chat ended. Press Enter to return to menu]");
                running.set(false); // Stop the chat loop
                break;

            default:
                // Ignore other event types
                break;
        }
    }

    private ChatPacket extractChatPacket(SocketMessage message) {
        if (message.getData() instanceof ChatPacket) {
            return (ChatPacket) message.getData();
        }
        // Handle case where data is a LinkedTreeMap from Gson
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String json = gson.toJson(message.getData());
            return gson.fromJson(json, ChatPacket.class);
        } catch (Exception e) {
            return null;
        }
    }
}
