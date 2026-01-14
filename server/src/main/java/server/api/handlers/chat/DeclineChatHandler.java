package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;

import java.net.Socket;

/**
 * Handles declining a chat request.
 */
public class DeclineChatHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null) {
            sendError(clientSocket, "Invalid decline request");
            return;
        }

        String declinerEmail = packet.getMessage(); // Client sends myEmail as message currently? No, wait.
        // Client previously sent: Message = myEmail.
        // Now client will send: SenderId = myEmail, Message = targetRequesterEmail.

        // Wait, check ChatRequestsCli logic. Previously:
        // ChatPacket packet = new ChatPacket(null, null, myEmail);

        // So packet.getMessage() WAS declinerEmail.
        // We should change this to standard: SenderId = decliner, Message = requester.

        String message = packet.getMessage();
        if (message == null || !message.contains(":")) {
            sendError(clientSocket, "Invalid decline format");
            return;
        }

        String[] parts = message.split(":");
        declinerEmail = parts[0];
        String targetRequesterEmail = parts[1];

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.declineChat(declinerEmail, targetRequesterEmail);
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.CHAT_MESSAGE, message));
    }
}
