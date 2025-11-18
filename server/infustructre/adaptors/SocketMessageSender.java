package server.infustructre.adaptors;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public interface SocketMessageSender {
    void sendToSockets(Map<String, Socket> sockets, String message);

    void sendToSocket(Socket socket, String message);

    void sendMessagesToSocket(Socket socket, List<String> messages);
}
