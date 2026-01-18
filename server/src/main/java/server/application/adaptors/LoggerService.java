package server.application.adaptors;

import java.util.List;
import server.domain.LogEntry;
import shareddto.reporting.SystemEventLogDto;

public interface LoggerService {
    // Internal use
    List<LogEntry> getLogs();

    // Export for client (returns JSON of SystemEventLogDto)
    SystemEventLogDto getSystemLogs();
}