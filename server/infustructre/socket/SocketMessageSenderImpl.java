package server.infustructre.socket;

import server.infustructre.adaptors.SocketMessageSender;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class SocketMessageSenderImpl implements SocketMessageSender {

    @Override
    public void sendToSockets(Map<String, Socket> sockets, String message) {
        if (sockets == null) {
            throw new IllegalArgumentException("Sockets map must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }

        for (Map.Entry<String, Socket> entry : sockets.entrySet()) {
            String email = entry.getKey();
            Socket socket = entry.getValue();

            try {
                sendToSocket(socket, message);
                System.out.println("Message sent to socket for user: " + email);
            } catch (Exception e) {
                System.err.println("Failed to send message to user " + email + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void sendToSocket(Socket socket, String message) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }

        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println(message);
            printStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send message to socket: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMessagesToSocket(Socket socket, List<String> messages) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket must not be null");
        }
        if (messages == null) {
            throw new IllegalArgumentException("Messages must not be null");
        }

        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            for (String message : messages) {
                printStream.println(message);
            }
            printStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send messages to socket: " + e.getMessage(), e);
        }
    }
}
