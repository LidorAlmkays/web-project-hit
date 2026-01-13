package frontend.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
            
            if (response == null) {
                throw new RuntimeException("Server returned null response");
            }
            
            Object data = response.getData();
            
            if (data == null) {
                throw new RuntimeException("Server returned null data");
            }
            
            if (data instanceof String && ((String) data).startsWith("ERROR")) {
                throw new RuntimeException("Server returned error: " + data);
            }
            
            String jsonData = data instanceof String ? (String) data : gson.toJson(data);
            return gson.fromJson(jsonData, SystemEventLogDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Transport error: " + e.getMessage(), e);
        }
    }
}