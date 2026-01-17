package frontend.services;

import java.io.IOException;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.reporting.SystemEventLogDto;

public class FrontendLoggerService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendLoggerService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    public SystemEventLogDto fetchSystemLogs() throws IOException {
        SocketMessage response = clientTransport.send(EventType.GET_SYSTEM_LOGS_JSON, null);
        
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
        return gson.fromJson(json, SystemEventLogDto.class);
    }
}