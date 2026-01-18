package frontend.services;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.SalesStatsReportDto;

import java.io.IOException;
import java.util.UUID;

public class FrontendReportService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendReportService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    // --- Inventory ---
    public BranchInventoryReportDto getBranchInventoryReport(UUID branchId) throws IOException {
        SocketMessage response = clientTransport.send(EventType.GET_BRANCH_INVENTORY_REPORT, branchId.toString());
        
        return extractData(response, BranchInventoryReportDto.class);
    }

    // --- Sales Stats ---
    public SalesStatsReportDto getSalesByBranchReport() throws IOException {
        SocketMessage response = clientTransport.send(EventType.GET_SALES_STATS_BRANCH, null);
        return extractData(response, SalesStatsReportDto.class);
    }

    public SalesStatsReportDto getSalesByProductReport() throws IOException {
        SocketMessage response = clientTransport.send(EventType.GET_SALES_STATS_PRODUCT, null);
        return extractData(response, SalesStatsReportDto.class);
    }

    private <T> T extractData(SocketMessage response, Class<T> type) {
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
        return gson.fromJson(json, type);
    }
}