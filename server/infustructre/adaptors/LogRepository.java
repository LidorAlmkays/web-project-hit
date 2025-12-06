package server.infustructre.adaptors;

import java.util.List;

public interface LogRepository {

    void info(String message);

    void error(Error error);

    List<String> getLogs();
}
