package server.application.services;

import server.application.adaptors.SocketManager;
import server.infustructre.adaptors.SocketMessageSender;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SocketManagerImpl implements SocketManager {
    private final SocketMessageSender socketMessageSender;
    // mapping emails to their sockets - "who's online" list
    private static final Map<String, Socket> emailToSocket = Collections.synchronizedMap(new HashMap<>());

    public SocketManagerImpl(SocketMessageSender socketMessageSender) {
        if (socketMessageSender == null) {
            throw new IllegalArgumentException("socketMessageSender cannot be null");
        }
        this.socketMessageSender = socketMessageSender;
    }

    @Override
    public void bindSocket(String emailAddress, Socket socket) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("email address is required");
        }
        if (socket == null) {
            throw new IllegalArgumentException("socket cannot be null");
        }

        emailToSocket.put(emailAddress, socket);
        System.out.println("Email " + emailAddress + " bound to socket");
    }

    @Override
    public void unbindSocket(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        Socket removedSocket = emailToSocket.remove(emailAddress);
        if (removedSocket != null) {
            System.out.println("Socket unbound for user: " + emailAddress);
        }
    }

    @Override
    public Optional<Socket> getSocketByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("email address is required");
        }

        Socket socket = emailToSocket.get(emailAddress);
        return Optional.ofNullable(socket);
    }

    @Override
    public boolean isEmailBound(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        return emailToSocket.containsKey(emailAddress);
    }

    @Override
    public void sendMessageToEmail(String emailAddress, String message) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("email address is required");
        }
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        Optional<Socket> socketOpt = getSocketByEmail(emailAddress);
        if (socketOpt.isPresent()) {
            socketMessageSender.sendToSocket(socketOpt.get(), message);
            System.out.println("Sent a message to user: " + emailAddress);

        } else {
            System.err.println("User not logged in, message not sent: " + emailAddress);
        }
    }

    @Override
    public void sendMessagesToEmail(String emailAddress, List<String> messages) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("email address is required");
        }
        if (messages == null) {
            throw new IllegalArgumentException("messages cannot be null");
        }

        Optional<Socket> socketOpt = getSocketByEmail(emailAddress);
        if (socketOpt.isPresent()) {
            Socket socket = socketOpt.get();
            socketMessageSender.sendMessagesToSocket(socket, messages);
            System.out.println("Sent " + messages.size() + " messages to user: " + emailAddress);
        } else {
            System.err.println("User not logged in, messages not sent: " + emailAddress);
        }
    }
}
