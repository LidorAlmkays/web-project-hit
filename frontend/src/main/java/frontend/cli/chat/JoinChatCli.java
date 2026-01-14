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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CLI for managers to view and join active chat sessions.
 */
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

        // Fetch active chats from server
        ChatPacket requestPacket = new ChatPacket(null, null, myEmail);
        SocketMessage response = client.send(EventType.GET_ACTIVE_CHATS, requestPacket);

        List<ActiveChatInfo> chats = null;
        if (response != null && response.getData() != null) {
            ChatPacket responsePacket = extractChatPacket(response);
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

        // Show numbered list
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
        // Send join request: "managerEmail:sessionId"
        ChatPacket joinPacket = new ChatPacket(null, null, myEmail + ":" + sessionId);
        client.sendOnly(EventType.MANAGER_JOIN, joinPacket);

        System.out.println("Joining chat...\n");
        enterChatMode(client, scanner, myEmail);
        return CliResult.BACK;
    }

    private void enterChatMode(IClientTransport client, Scanner scanner, String myEmail) throws IOException {
        System.out.println("--- Chat Mode (Manager) ---");
        System.out.println("Type your message and press Enter to send.");
        System.out.println("Type /exit to leave the chat.\n");

        AtomicBoolean running = new AtomicBoolean(true);

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

        while (running.get()) {
            System.out.print("> ");
            String message = scanner.nextLine();

            if (!running.get())
                break;

            if (message.equalsIgnoreCase("/exit")) {
                ChatPacket closePacket = new ChatPacket(null, null, myEmail);
                client.sendOnly(EventType.CHAT_CLOSE, closePacket);
                System.out.println("Left the chat.");
                running.set(false);
            } else if (!message.trim().isEmpty()) {
                ChatPacket msgPacket = new ChatPacket(null, null, myEmail + ":" + message);
                client.sendOnly(EventType.CHAT_MESSAGE, msgPacket);
                System.out.println("You: " + message);
            }
        }

        running.set(false);
        listenerThread.interrupt();
    }

    private void handleIncomingMessage(SocketMessage message, AtomicBoolean running) {
        EventType eventType = message.getEventType();

        switch (eventType) {
            case CHAT_MESSAGE:
                if (message.getData() != null) {
                    ChatPacket packet = extractChatPacket(message);
                    if (packet != null && packet.getMessage() != null) {
                        System.out.println(packet.getMessage());
                    }
                }
                break;

            case CHAT_CLOSE:
                System.out.println("\n[Chat ended. Press Enter to return to menu]");
                running.set(false);
                break;

            default:
                break;
        }
    }

    private ChatPacket extractChatPacket(SocketMessage message) {
        if (message.getData() instanceof ChatPacket) {
            return (ChatPacket) message.getData();
        }
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String json = gson.toJson(message.getData());
            return gson.fromJson(json, ChatPacket.class);
        } catch (Exception e) {
            return null;
        }
    }
}
