package shareddto.reporting;

import java.io.Serializable;

public class LogEntryDto implements Serializable {
    private String timestamp;
    private String level;
    private String type;
    private String actor;
    private String message;

    public LogEntryDto(String timestamp, String level, String type, String actor, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.type = type;
        this.actor = actor;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getType() { return type; }
    public String getActor() { return actor; }
    public String getMessage() { return message; }
}