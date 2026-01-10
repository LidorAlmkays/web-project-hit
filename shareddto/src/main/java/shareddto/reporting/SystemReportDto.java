package shareddto.reporting;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SystemReportDto implements Serializable{
    private String reportDate;
    private int totalLogEntries;
    private List<LogEntryDto> logs;
    
    public SystemReportDto(String reportDate, int totalLogEntries, List<LogEntryDto> logs) {
        this.reportDate = reportDate;
        this.totalLogEntries = totalLogEntries;
        this.logs = logs; 
    }

    public String getReportDate() { return reportDate; }
    public int getTotalLogEntries() { return totalLogEntries; }
    public List<LogEntryDto> getLogs() { return logs; }
}
