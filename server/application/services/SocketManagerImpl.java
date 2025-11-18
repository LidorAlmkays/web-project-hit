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
    private static final Map<String, Socket> emailToSocket = Collections.synchronizedMap(new HashMap<>());

    public SocketManagerImpl(SocketMessageSender socketMessageSender) {
        if (socketMessageSender == null) {
            throw new IllegalArgumentException("SocketMessageSender must not be null");
        }
        this.socketMessageSender = socketMessageSender;
    }

    @Override
    public void bindSocket(String emailAddress, Socket socket) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }
        if (socket == null) {
            throw new IllegalArgumentException("Socket must not be null");
        }

        emailToSocket.put(emailAddress, socket);
        System.out.println("Email " + emailAddress + " bound to socket");
    }

    @Override
    public void unbindSocket(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }

        Socket removedSocket = emailToSocket.remove(emailAddress);
        if (removedSocket != null) {
            System.out.println("Socket unbound for user: " + emailAddress);
        }
    }

    @Override
    public Optional<Socket> getSocketByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }

        Socket socket = emailToSocket.get(emailAddress);
        return Optional.ofNullable(socket);
    }

    @Override
    public boolean isEmailBound(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }

        return emailToSocket.containsKey(emailAddress);
    }

    @Override
    public void sendMessageToEmail(String emailAddress, String message) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
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
            throw new IllegalArgumentException("Email address must not be null or empty");
        }
        if (messages == null) {
            throw new IllegalArgumentException("Messages must not be null");
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
