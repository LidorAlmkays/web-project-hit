package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.net.Socket;

public class JoinChatHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null) {
            sendError(clientSocket, "Invalid request");
            return;
        }

        // Message format: "managerEmail:sessionId"
        String message = packet.getMessage();
        if (message == null || !message.contains(":")) {
            sendError(clientSocket, "Invalid join format");
            return;
        }

        String[] parts = message.split(":", 2);
        String managerEmail = parts[0];
        String sessionId = parts[1];

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.joinChat(managerEmail, sessionId);
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        ChatPacket errorPacket = new ChatPacket(null, null, "ERROR:" + message);
        sendMessage(clientSocket, new SocketMessage(EventType.MANAGER_JOIN, errorPacket));
    }
}
