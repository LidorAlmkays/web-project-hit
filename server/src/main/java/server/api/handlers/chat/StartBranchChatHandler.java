package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.net.Socket;
import java.util.UUID;

/**
 * Handles CHAT_REQUEST - starts a chat with another branch.
 */
public class StartBranchChatHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null) {
            sendError(clientSocket, "Invalid chat request");
            return;
        }

        // Extract requester email from message field, target branch ID from receiverId
        String requesterEmail = packet.getMessage();
        UUID targetBranchId = packet.getReceiverId();

        if (requesterEmail == null || targetBranchId == null) {
            sendError(clientSocket, "Missing requester email or target branch");
            return;
        }

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.requestBranchChat(requesterEmail, targetBranchId);

        // Response is sent by ChatManager via the socket
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.CHAT_REQUEST, message));
    }
}
