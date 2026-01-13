package frontend.services;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.SalesStatsReportDto;

import java.util.UUID;

public class FrontendReportService {

    private final IClientTransport clientTransport;
    private final Gson gson;

    public FrontendReportService(IClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.gson = new Gson();
    }

    // --- Inventory ---
    public BranchInventoryReportDto getBranchInventoryReport(UUID branchId) {
        
        String jsonResponse = clientTransport.send"GET_BRANCH_INVENTORY_REPORT", branchId.toString());
        
        if (jsonResponse.startsWith("ERROR")) {
            throw new RuntimeException("Server error: " + jsonResponse);
        }
        return gson.fromJson(jsonResponse, BranchInventoryReportDto.class);
    }

    // --- Sales Stats ---
    public SalesStatsReportDto getSalesByBranchReport() {
        return fetchStatsReport("GET_SALES_STATS_BRANCH");
    }

    public SalesStatsReportDto getSalesByProductReport() {
        return fetchStatsReport("GET_SALES_STATS_PRODUCT");
    }

    private SalesStatsReportDto fetchStatsReport(String messageType) {
        
        String jsonResponse = clientTransport.send(messageType);
        
        if (jsonResponse.startsWith("ERROR")) {
            throw new RuntimeException("Server error: " + jsonResponse);
        }
        return gson.fromJson(jsonResponse, SalesStatsReportDto.class);
    }
}