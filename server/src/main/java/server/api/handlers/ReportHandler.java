package server.api.handlers;

import server.application.adaptors.ReportService;
import server.application.services.ReportServiceImpl;
import java.net.Socket;
import java.util.UUID;

public class ReportHandler extends AbstractSocketHandler {

    private final ReportService reportService;
    private final ReportType type;

    public enum ReportType {
        DAILY_JSON, DAILY_WORD, BRANCH_JSON, BRANCH_WORD,
        SALES_STATS_BRANCH, SALES_STATS_PRODUCT
    }

    public ReportHandler(ReportService reportService, ReportType type) {
        this.reportService = reportService;
        this.type = type;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String response = "";

        ReportServiceImpl serviceImpl = (ReportServiceImpl) reportService; 

        switch (type) {
            case DAILY_JSON:
            case DAILY_WORD: 
                response = serviceImpl.getDailySystemReportJson();
                break;
                
            case BRANCH_JSON:
            case BRANCH_WORD:
                if (!(data instanceof String)) {
                    throw new IllegalArgumentException("Error: Branch report requires a Branch ID (String).");
                }
                try { 
                    response = serviceImpl.getBranchInventoryReportJson(UUID.fromString((String) data));
                } catch (IllegalArgumentException e) {
                    response = "{ \"error\": \"Invalid Branch ID format\" }";
                }
                break;

            case SALES_STATS_BRANCH:
                response = serviceImpl.getSalesStatsByBranchJson();
                break;

            case SALES_STATS_PRODUCT:
                response = serviceImpl.getSalesStatsByProductJson();
                break;
        }

        send(clientSocket, response);
    }
}