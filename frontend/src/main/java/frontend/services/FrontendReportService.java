package frontend.application.services;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.reporting.SystemReportDto;

public class FrontendReportService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendReportService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    public SystemReportDto getSystemReport() {
        clientTransport.sendMessage("GET_SYSTEM_REPORT");

        String jsonResponse = clientTransport.receiveMessage();

        if (jsonResponse.startsWith("ERROR")) {
            throw new RuntimeException("Server returned error: " + jsonResponse);
        }

        return gson.fromJson(jsonResponse, SystemReportDto.class);
    }
}