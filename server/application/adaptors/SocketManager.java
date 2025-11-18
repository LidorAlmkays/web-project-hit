package server.application.adaptors;

import java.net.Socket;
import java.util.List;
import java.util.Optional;

public interface SocketManager {
    void bindSocket(String emailAddress, Socket socket);

    void unbindSocket(String emailAddress);

    Optional<Socket> getSocketByEmail(String emailAddress);

    boolean isEmailBound(String emailAddress);

    void sendMessageToEmail(String emailAddress, String message);

    void sendMessagesToEmail(String emailAddress, List<String> messages);
}
