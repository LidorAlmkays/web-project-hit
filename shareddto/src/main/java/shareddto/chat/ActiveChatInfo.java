package shareddto.chat;

/**
 * DTO for active chat session information.
 */
public class ActiveChatInfo {
    private String sessionId;
    private String displayString;
    private long startTime;

    public ActiveChatInfo() {
    }

    public ActiveChatInfo(String sessionId, String displayString, long startTime) {
        this.sessionId = sessionId;
        this.displayString = displayString;
        this.startTime = startTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
