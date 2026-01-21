package server.application.adaptors;

import java.util.List;
import server.domain.LogEntry;
import shareddto.reporting.SystemEventLogDto;

public interface LoggerService {
    List<LogEntry> getLogs();

    SystemEventLogDto getSystemLogs();
}
