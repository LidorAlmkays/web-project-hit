package server.infustructre.adaptors;

import server.domain.LogEntry;
import java.util.List;

public interface LogRepository {

    void save(LogEntry entry);
    List<LogEntry> findAll();

    void info(LogEntry.LogType type, String message);
    void info(LogEntry.LogType type, String userId, String message);
    
    void error(LogEntry.LogType type, String message);
    void error(LogEntry.LogType type, String userId, String message);
}
