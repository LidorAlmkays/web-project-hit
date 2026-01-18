package shareddto.reporting;

import java.io.Serializable;
import java.util.List;

public class ChatHistoryDto implements Serializable {
    private String generatedDate;
    private int totalChats;
    private List<ChatSessionDto> chatSessions;
    
    public ChatHistoryDto(String generatedDate, int totalChats, List<ChatSessionDto> chatSessions) {
        this.generatedDate = generatedDate;
        this.totalChats = totalChats;
        this.chatSessions = chatSessions;
    }

    public String getGeneratedDate() { return generatedDate; }
    public int getTotalChats() { return totalChats; }
    public List<ChatSessionDto> getChatSessions() { return chatSessions; }
}
