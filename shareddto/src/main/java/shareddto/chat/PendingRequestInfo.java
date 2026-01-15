package shareddto.chat;

import java.io.Serializable;

public class PendingRequestInfo implements Serializable {
    private String requesterEmail;
    private long timestamp;

    public PendingRequestInfo(String requesterEmail, long timestamp) {
        this.requesterEmail = requesterEmail;
        this.timestamp = timestamp;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
