package server.api.dto;

public class SocketMessage {
    private EventType eventType;
    private Object data;

    public SocketMessage() {
    }

    public SocketMessage(EventType eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
