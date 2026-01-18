package frontend.services;

import java.io.IOException;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.reporting.ChatHistoryDto;

public class FrontendChatService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendChatService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    public ChatHistoryDto fetchChatHistory() throws IOException {
        SocketMessage response = clientTransport.send(EventType.GET_CHAT_HISTORY_JSON, null);
        
        if (response == null) {
            throw new RuntimeException("Error: No response from server.");
        }

        if (response.getEventType() == EventType.ERROR) {
            throw new RuntimeException("Server Error: " + response.getData());
        }

        if (response.getData() == null) {
            throw new RuntimeException("Error: Empty data received.");
        }

        String json = gson.toJson(response.getData());
        return gson.fromJson(json, ChatHistoryDto.class);
    }
}
