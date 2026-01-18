package server.api.handlers;

import server.chat.ChatManager;
import shareddto.SocketMessage;
import shareddto.EventType;
import java.net.Socket;

public class ChatHistoryHandler extends AbstractSocketHandler {

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            ChatManager chatManager = ChatManager.getInstance();
            var chatHistory = chatManager.getChatHistory();

            SocketMessage response = new SocketMessage(
                EventType.GET_CHAT_HISTORY_JSON, 
                chatHistory
            );

            sendMessage(clientSocket, response);
        } catch (Exception e) {
            e.printStackTrace();
            SocketMessage errorResponse = new SocketMessage(
                EventType.ERROR,
                "Failed to retrieve chat history: " + e.getMessage()
            );
            sendMessage(clientSocket, errorResponse);
        }
    }
}
