package shareddto.reporting;

import java.io.Serializable;

public class ChatHistoryEntryDto implements Serializable {
    private String timestamp;
    private String senderEmail;
    private String message;

    public ChatHistoryEntryDto(String timestamp, String senderEmail, String message) {
        this.timestamp = timestamp;
        this.senderEmail = senderEmail;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public String getSenderEmail() { return senderEmail; }
    public String getMessage() { return message; }
}
