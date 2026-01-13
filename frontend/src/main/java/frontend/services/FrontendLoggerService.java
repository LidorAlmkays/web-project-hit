package frontend.services;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.reporting.SystemEventLogDto;

public class FrontendLoggerService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendLoggerService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    public SystemEventLogDto fetchSystemLogs() {
        clientTransport.sendMessage("GET_SYSTEM_LOGS_DOCUMENT");
        
        String jsonResponse = clientTransport.receiveMessage();
        if (jsonResponse.startsWith("ERROR")) {
            throw new RuntimeException("Server returned error: " + jsonResponse);
        }
        
        return gson.fromJson(jsonResponse, SystemEventLogDto.class);
    }
}