package server.api.handlers;

import server.application.adaptors.ReportService;
import shareddto.EventType;
import shareddto.SocketMessage;
import java.net.Socket;
import java.util.UUID;

public class ReportHandler extends AbstractSocketHandler {

    private final ReportService reportService;
    private final ReportType type;

    public enum ReportType {
        BRANCH_INVENTORY, SALES_STATS_BRANCH, SALES_STATS_PRODUCT
    }

    public ReportHandler(ReportService reportService, ReportType type) {
        this.reportService = reportService;
        this.type = type;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        SocketMessage request = (SocketMessage) data;
        Object responseData = null;
        EventType responseType = null;

        try {
            switch (type) {
                case BRANCH_INVENTORY:
                    if (request.getData() != null) {
                        UUID branchId = UUID.fromString(request.getData().toString());
                        responseData = reportService.getBranchInventoryReport(branchId);
                        responseType = EventType.GET_BRANCH_INVENTORY_REPORT;
                    }
                    break;

                case SALES_STATS_BRANCH:
                    responseData = reportService.getSalesStatsByBranch();
                    responseType = EventType.GET_SALES_STATS_BRANCH;
                    break;

                case SALES_STATS_PRODUCT:
                    responseData = reportService.getSalesStatsByProduct();
                    responseType = EventType.GET_SALES_STATS_PRODUCT;
                    break;
            }

            SocketMessage response = new SocketMessage(responseType, responseData);
            sendMessage(clientSocket, response);

        } catch (Exception e) {
            e.printStackTrace();
            SocketMessage errorResponse = new SocketMessage(
                EventType.ERROR, 
                "Failed to fetch system report: " + e.getMessage()
            );
            sendMessage(clientSocket, errorResponse);
        }
    }
}