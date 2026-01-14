package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.net.Socket;

/**
 * Handles accepting a chat request.
 */
public class AcceptChatHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null) {
            sendError(clientSocket, "Invalid accept request");
            return;
        }

        String message = packet.getMessage();
        if (message == null || !message.contains(":")) {
            sendError(clientSocket, "Invalid accept format");
            return;
        }

        String[] parts = message.split(":");
        String accepterEmail = parts[0];
        String targetRequesterEmail = parts[1];

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.acceptChat(accepterEmail, targetRequesterEmail);
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.CHAT_MESSAGE, message));
    }
}
