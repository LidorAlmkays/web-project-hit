package server.application.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import server.application.adaptors.LoggerService;
import server.domain.LogEntry;
import server.infustructre.adaptors.LogRepository;
import shareddto.reporting.LogEntryDto;
import shareddto.reporting.SystemEventLogDto;

public class LoggerServiceImpl implements LoggerService {

    private final LogRepository logRepository;

    public LoggerServiceImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public List<LogEntry> getLogs() {
        return logRepository.findAll();
    }

    @Override
    public SystemEventLogDto getSystemLogs() {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated System Event Log");
        List<LogEntry> rawLogs = getLogsFromLast24Hours();

        List<LogEntryDto> dtos = rawLogs.stream()
            .map(log -> new LogEntryDto(
                log.getTimestamp().toString(),
                log.getLevel().toString(),
                log.getType().toString(),
                log.getEmail(), // Actor
                log.getMessage()
            ))
            .collect(Collectors.toList());

        return new SystemEventLogDto(
            LocalDateTime.now().toString(),
            dtos.size(),
            dtos
        );
    }

    private List<LogEntry> getLogsFromLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return logRepository.findAll().stream()
                .filter(log -> log.getTimestamp() != null && log.getTimestamp().isAfter(yesterday))
                .collect(Collectors.toList());
    }
}