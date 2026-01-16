package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.chat.ChatPacket;

import java.net.Socket;

/**
 * Handles CHAT_CLOSE - closes an active chat session.
 */
public class CloseChatHandler extends AbstractSocketHandler {

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

        // Extract the email of the user closing the chat
        String email = packet.getMessage(); // Using message field to pass email

        if (email != null && !email.isEmpty()) {
            ChatManager chatManager = ChatManager.getInstance();
            chatManager.closeChat(email);
        }
    }
}
