package server.api.handlers;

import server.application.adaptors.ReportService;
import java.net.Socket;
import java.util.UUID;

public class ReportHandler extends AbstractSocketHandler {

    private final ReportService reportService;
    private final ReportType type;

    public enum ReportType {
        BRANCH_INVENTORY,
        SALES_STATS_BRANCH,
        SALES_STATS_PRODUCT
    }

    public ReportHandler(ReportService reportService, ReportType type) {
        this.reportService = reportService;
        this.type = type;
    }

    @Override
    public void handle(Object data, Socket clientSocket) {
        String response = "";

        switch (type) {
            case BRANCH_INVENTORY:
                if (data == null) {
                    send(clientSocket, "{ \"error\": \"Branch ID is required\" }");
                    return;
                }
                try {
                    UUID branchId = UUID.fromString(data.toString());
                    response = reportService.getBranchInventoryReport(branchId);
                } catch (IllegalArgumentException e) {
                    response = "{ \"error\": \"Invalid Branch ID format\" }";
                }
                break;

            case SALES_STATS_BRANCH:
                response = reportService.getSalesStatsByBranch();
                break;

            case SALES_STATS_PRODUCT:
                response = reportService.getSalesStatsByProduct();
                break;
        }

        send(clientSocket, response);
    }
}