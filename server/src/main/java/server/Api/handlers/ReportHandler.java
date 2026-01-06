package server.api.handlers;

import server.application.adaptors.ReportService;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ReportHandler implements SocketHandler {
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
                if (data instanceof String) {
                    response = reportService.getBranchInventoryReportJson(UUID.fromString((String) data));
                }
                break;
            case BRANCH_WORD:
                if (data instanceof String) {
                    response = reportService.getBranchInventoryReportWord(UUID.fromString((String) data));
                }
                break;
        }

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeUTF(response);
        out.flush();
    }
}