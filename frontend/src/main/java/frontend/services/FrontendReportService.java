package frontend.application.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import frontend.transport.IClientTransport;
import shareddto.reporting.SystemReportDto;
import java.lang.reflect.Type;
import java.util.Map;

public class FrontendReportService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendReportService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    public SystemReportDto getSystemReport() {
        clientTransport.sendMessage("GET_SYSTEM_REPORT");
        return fetchReportAndParse("GET_DAILY_REPORT_WORD", SystemReportDto.class);
    }


    public Map<String, Object> getSalesByBranchReport() {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return fetchReportAndParse("GET_SALES_STATS_BRANCH", type);
    }

    public Map<String, Object> getSalesByProductReport() {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return fetchReportAndParse("GET_SALES_STATS_PRODUCT", type);
    }

    private <T> T fetchReportAndParse(String messageType, Type typeOfT) {
        clientTransport.sendMessage(messageType);
        String jsonResponse = clientTransport.receiveMessage();

        if (jsonResponse.startsWith("ERROR")) {
            throw new RuntimeException("Server returned error: " + jsonResponse);
        }
        return gson.fromJson(jsonResponse, typeOfT);
    }
}