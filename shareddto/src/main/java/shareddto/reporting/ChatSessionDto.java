package shareddto.reporting;

import java.io.Serializable;
import java.util.List;

public class ChatSessionDto implements Serializable {
    private String fileName;
    private String startTime;
    private String endTime;
    private List<String> participants;
    private List<ChatHistoryEntryDto> messages;

    public ChatSessionDto(String fileName, String startTime, String endTime, 
                         List<String> participants, List<ChatHistoryEntryDto> messages) {
        this.fileName = fileName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participants = participants;
        this.messages = messages;
    }

    public String getFileName() { return fileName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public List<String> getParticipants() { return participants; }
    public List<ChatHistoryEntryDto> getMessages() { return messages; }
}
