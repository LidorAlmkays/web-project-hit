package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.chat.ChatPacket;

import java.net.Socket;

public class SendMessageHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null) {
            return;
        }

        // Extract sender email and message
        // Note: We need to identify the sender. For now using message field format
        // "email:message"
        String messageContent = packet.getMessage();
        if (messageContent == null || !messageContent.contains(":")) {
            return;
        }

        String[] parts = messageContent.split(":", 2);
        String senderEmail = parts[0];
        String message = parts.length > 1 ? parts[1] : "";

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.handleMessage(senderEmail, message);
    }
}
