package server.api.handlers.chat;

import server.api.handlers.AbstractSocketHandler;
import server.chat.ChatManager;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.chat.ChatPacket;
import shareddto.chat.PendingRequestInfo;

import java.net.Socket;
import java.util.List;

public class GetPendingRequestsHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        ChatPacket packet;
        if (data instanceof ChatPacket) {
            packet = (ChatPacket) data;
        } else {
            packet = gson.fromJson(gson.toJson(data), ChatPacket.class);
        }

        if (packet == null) {
            sendMessage(clientSocket, new SocketMessage(EventType.GET_PENDING_REQUESTS, null));
            return;
        }

        String email = packet.getMessage();
        if (email == null || email.isEmpty()) {
            sendMessage(clientSocket, new SocketMessage(EventType.GET_PENDING_REQUESTS, null));
            return;
        }

        ChatManager chatManager = ChatManager.getInstance();
        List<PendingRequestInfo> requests = chatManager.getPendingRequestInfos(email);

        // Serialize list to JSON string
        String jsonList = gson.toJson(requests);

        // Return the JSON list in the message field
        ChatPacket response = new ChatPacket(null, null, jsonList);
        sendMessage(clientSocket, new SocketMessage(EventType.GET_PENDING_REQUESTS, response));
    }
}
