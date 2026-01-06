package server.application.adaptors;

import java.util.List;

import server.domain.LogEntry;

public interface LoggerService {
    List<LogEntry> getLogs();
}
