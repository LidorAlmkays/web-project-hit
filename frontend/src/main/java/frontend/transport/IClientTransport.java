package frontend.transport;

import shareddto.EventType;
import shareddto.SocketMessage;

import java.io.Closeable;
import java.io.IOException;

public interface IClientTransport extends Closeable {
    SocketMessage send(EventType eventType, Object data) throws IOException;
    void sendOnly(EventType eventType, Object data) throws IOException;

    SocketMessage receive() throws IOException;
}
