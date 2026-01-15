package frontend.cli.chat;

import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChatModeHelper {

    public static void enterChatMode(IClientTransport client, Scanner scanner, String myEmail) throws IOException {
        enterChatMode(client, scanner, myEmail, "Chat");
    }

    public static void enterChatMode(IClientTransport client, Scanner scanner, String myEmail, String modeLabel)
            throws IOException {
        System.out.println("\n--- " + modeLabel + " Mode ---");
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
        listenerThread.start();

        while (running.get()) {
            System.out.print("> ");
            String message = scanner.nextLine();

            if (!running.get())
                break;

            if (message.equalsIgnoreCase("/exit")) {
                ChatPacket closePacket = new ChatPacket(null, null, myEmail);
                client.sendOnly(EventType.CHAT_CLOSE, closePacket);
                System.out.println("Chat closed.");
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

    private static void handleIncomingMessage(SocketMessage message, AtomicBoolean running) {
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

    public static ChatPacket extractChatPacket(SocketMessage message) {
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
