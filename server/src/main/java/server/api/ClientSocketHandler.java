package server.api;

import shareddto.EventType;
import shareddto.SocketMessage;
import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSocketHandler implements Runnable {
    private static final Gson gson = new Gson();
    private final Socket clientSocket;
    private final HandlerFactory handlerFactory;
    private DataInputStream inputStream;
    private boolean running;

    public ClientSocketHandler(Socket clientSocket, HandlerFactory handlerFactory) {
        this.clientSocket = clientSocket;
        this.handlerFactory = handlerFactory;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            while (running && clientSocket.isConnected() && !clientSocket.isClosed()) {
                String jsonMessage = inputStream.readUTF();

                if (jsonMessage == null || jsonMessage.isEmpty()) {
                    System.out.println("Client disconnected (empty message)");
                    break;
                }

                System.out.println("Received message: " + jsonMessage);

                SocketMessage message = null;
                try {
                    message = gson.fromJson(jsonMessage, SocketMessage.class);
                    System.out.println("Parsed message - EventType: " +
                            (message != null ? message.getEventType() : "null") +
                            ", Data: " + (message != null ? message.getData() : "null"));
                } catch (Exception e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                if (message == null || message.getEventType() == null) {
                    System.out.println("Invalid message format - missing eventType");
                    continue;
                }

                EventType eventType = message.getEventType();
                server.api.handlers.SocketHandler handler = handlerFactory.createHandler(eventType);

                handler.handle(message.getData(), clientSocket);
            }
        } catch (Exception e) {
            System.err.println("Error in ClientSocketHandler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket resources: " + e.getMessage());
        }
        System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
    }
}
