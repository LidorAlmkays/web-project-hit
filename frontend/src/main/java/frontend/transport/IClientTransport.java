package frontend.transport;

import shareddto.EventType;
import shareddto.SocketMessage;

import java.io.Closeable;
import java.io.IOException;

/**
 * Abstraction for CLI transport implementations (socket or mock).
 */
public interface IClientTransport extends Closeable {
    SocketMessage send(EventType eventType, Object data) throws IOException;
    SocketMessage receive() throws IOException;
}
