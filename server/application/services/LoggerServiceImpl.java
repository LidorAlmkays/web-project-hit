package server.application.services;

import java.util.List;

import server.application.adaptors.LoggerService;
import server.domain.LogEntry;
import server.infustructre.adaptors.LogRepository;

public class LoggerServiceImpl implements LoggerService {

    LogRepository logRepository;

    public LoggerServiceImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public List<LogEntry> getLogs() {
        return logRepository.findAll();
    }
}
