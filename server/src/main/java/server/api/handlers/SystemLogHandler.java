package server.api.handlers;

import server.application.adaptors.LoggerService;
import shareddto.SocketMessage;
import shareddto.EventType;
import shareddto.reporting.SystemEventLogDto;
import java.net.Socket;

public class SystemLogHandler extends AbstractSocketHandler {

    private final LoggerService loggerService;

    public SystemLogHandler(LoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            SystemEventLogDto logs = loggerService.getSystemLogs();

            SocketMessage response = new SocketMessage(
                EventType.GET_SYSTEM_LOGS_JSON, 
                logs
            );


            sendMessage(clientSocket, response);
        } catch (Exception e) {
            e.printStackTrace();
            SocketMessage errorResponse = new SocketMessage(
                EventType.ERROR, 
                "Failed to fetch system logs: " + e.getMessage()
            );
            sendMessage(clientSocket, errorResponse);
        }
    }
}