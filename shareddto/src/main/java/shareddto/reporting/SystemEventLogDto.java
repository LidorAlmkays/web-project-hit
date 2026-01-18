package shareddto.reporting;

import java.io.Serializable;
import java.util.List;

public class SystemEventLogDto implements Serializable {
    private String generatedDate;
    private int totalEntries;
    private List<LogEntryDto> logs;
    
    public SystemEventLogDto(String generatedDate, int totalEntries, List<LogEntryDto> logs) {
        this.generatedDate = generatedDate;
        this.totalEntries = totalEntries;
        this.logs = logs; 
    }

    public String getGeneratedDate() { return generatedDate; }
    public int getTotalEntries() { return totalEntries; }
    public List<LogEntryDto> getLogs() { return logs; }
}