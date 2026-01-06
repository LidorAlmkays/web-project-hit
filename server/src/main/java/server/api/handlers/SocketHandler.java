package server.api.handlers;

import java.net.Socket;

public interface SocketHandler {
    void handle(Object data, Socket clientSocket) throws Exception;
}
