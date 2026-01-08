package frontend.transport;

import com.google.gson.Gson;
import shareddto.EventType;
import shareddto.SocketMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Socket-based transport for the CLI.
 */
public class SocketClient implements IClientTransport {
    private static final Gson gson = new Gson();
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public SocketClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public SocketMessage send(EventType eventType, Object data) throws IOException {
        SocketMessage message = new SocketMessage(eventType, data);
        outputStream.writeUTF(gson.toJson(message));
        outputStream.flush();

        return receive();
    }

    @Override
    public SocketMessage receive() throws IOException {
        String responseJson = inputStream.readUTF();
        return gson.fromJson(responseJson, SocketMessage.class);
    }

    @Override
    public void close() throws IOException {
        try {
            inputStream.close();
        } finally {
            try {
                outputStream.close();
            } finally {
                socket.close();
            }
        }
    }
}
