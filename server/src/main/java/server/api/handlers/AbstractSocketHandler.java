package server.api.handlers;

import java.io.DataOutputStream;
import java.net.Socket;

import com.google.gson.Gson;

import shareddto.SocketMessage;

public abstract class AbstractSocketHandler implements SocketHandler {
    protected final Gson gson = new Gson();

    protected void sendMessage(Socket clientSocket, SocketMessage message) throws Exception {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeUTF(gson.toJson(message));
        out.flush();
    }
}
