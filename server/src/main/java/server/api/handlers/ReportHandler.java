package server.api.handlers;

import server.application.adaptors.ReportService;
import java.net.Socket;
import java.util.UUID;

public class ReportHandler extends AbstractSocketHandler {

    private final ReportService reportService;
    private final ReportType type;

    public enum ReportType {
        DAILY_JSON, DAILY_WORD, BRANCH_JSON, BRANCH_WORD
    }

    public ReportHandler(ReportService reportService, ReportType type) {
        this.reportService = reportService;
        this.type = type;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        String response = "";

        switch (type) {
            case DAILY_JSON:
                response = reportService.getDailySystemReportJson();
                break;
                
            case DAILY_WORD:
                response = reportService.getDailySystemReportWord();
                break;
                
            case BRANCH_JSON:
                if (!(data instanceof String)) {
                    throw new IllegalArgumentException("Error: Branch report requires a Branch ID (String).");
                }
                try { 
                    response = reportService.getBranchInventoryReportJson(UUID.fromString((String) data));
                } catch (IllegalArgumentException e) {
                    response = "{ \"error\": \"Invalid Branch ID format\" }";
                }
                break;
                
            case BRANCH_WORD:
                if (!(data instanceof String)) {
                    throw new IllegalArgumentException("Error: Branch report requires a Branch ID (String).");
                }
                response = reportService.getBranchInventoryReportWord(UUID.fromString((String) data));
                break;
        }

        send(clientSocket, response);
    }
}