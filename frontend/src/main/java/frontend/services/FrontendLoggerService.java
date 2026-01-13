package frontend.services;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.reporting.SystemEventLogDto;

import java.io.IOException;

public class FrontendLoggerService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendLoggerService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    public SystemEventLogDto fetchSystemLogs() {
        try {
            SocketMessage response = clientTransport.send(EventType.GET_SYSTEM_LOGS_DOCUMENT, null);
            Object data = response.getData();
            
            // Check if data is an error string
            if (data instanceof String && ((String) data).startsWith("ERROR")) {
                throw new RuntimeException("Server returned error: " + data);
            }
            
            // Parse the data as JSON - if it's already a String, use it directly; otherwise serialize it first
            String jsonData = data instanceof String ? (String) data : gson.toJson(data);
            return gson.fromJson(jsonData, SystemEventLogDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Transport error: " + e.getMessage(), e);
        }
    }
}