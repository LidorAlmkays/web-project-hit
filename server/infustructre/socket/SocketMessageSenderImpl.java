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
            throw new IllegalArgumentException("sockets cannot be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        for (Map.Entry<String, Socket> entry : sockets.entrySet()) {
            String email = entry.getKey();
            Socket socket = entry.getValue();

            try {
                sendToSocket(socket, message);
                System.out.println("Message sent to socket for user: " + email);
            } catch (Exception e) {
                // socket probably closed or network issue
                System.err.println("Failed to send message to user " + email + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void sendToSocket(Socket socket, String message) {
        if (socket == null) {
            throw new IllegalArgumentException("socket cannot be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println(message);
            printStream.flush(); // make sure it actually sends, don't be lazy
        } catch (IOException e) {
            // socket died or something went wrong - propagate it up
            throw new RuntimeException("Failed to send message to socket: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMessagesToSocket(Socket socket, List<String> messages) {
        if (socket == null) {
            throw new IllegalArgumentException("socket cannot be null");
        }
        if (messages == null) {
            throw new IllegalArgumentException("messages cannot be null");
        }

        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            // send all messages in order (for chat history replay)
            for (String message : messages) {
                printStream.println(message);
            }
            printStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send messages to socket: " + e.getMessage(), e);
        }
    }
}
