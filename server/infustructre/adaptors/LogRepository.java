package server.infustructre.adaptors;

import server.domain.LogEntry;
import java.util.List;

public interface LogRepository {

    void save(LogEntry entry);
    List<LogEntry> findAll();

    void info(String message);
    void error(Error error);
}
