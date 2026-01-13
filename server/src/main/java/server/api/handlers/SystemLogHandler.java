package server.api.handlers;

import server.application.adaptors.LoggerService;
import java.net.Socket;

public class SystemLogHandler extends AbstractSocketHandler {

    private final LoggerService loggerService;
    private final LogRequestType type;

    public enum LogRequestType {
        JSON, DOCUMENT
    }

    public SystemLogHandler(LoggerService loggerService, LogRequestType type) {
        this.loggerService = loggerService;
        this.type = type;
    }

    @Override
    public void handle(Object data, Socket clientSocket) {
        String response = "";

        switch (type) {
            case JSON:
            case DOCUMENT: 
                response = loggerService.getSystemLogsJson();
                break;
        }

        send(clientSocket, response);
    }
}