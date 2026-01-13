package server.application.adaptors;

import java.util.List;
import server.domain.LogEntry;

public interface LoggerService {
    // Internal use
    List<LogEntry> getLogs();

    // Export for client (returns JSON of SystemEventLogDto)
    String getSystemLogsJson();
}