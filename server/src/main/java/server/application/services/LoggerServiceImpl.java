package server.application.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import server.application.adaptors.LoggerService;
import server.domain.LogEntry;
import server.infustructre.adaptors.LogRepository;
import shareddto.reporting.LogEntryDto;
import shareddto.reporting.SystemEventLogDto;

public class LoggerServiceImpl implements LoggerService {

    private final LogRepository logRepository;
    private final Gson gson;

    public LoggerServiceImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                        new JsonPrimitive(src.toString()))
                .create();
    }

    @Override
    public List<LogEntry> getLogs() {
        return logRepository.findAll();
    }

    @Override
    public String getSystemLogsJson() {
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

        SystemEventLogDto eventLogDto = new SystemEventLogDto(
            LocalDateTime.now().toString(),
            dtos.size(),
            dtos
        );

        return gson.toJson(eventLogDto);
    }

    private List<LogEntry> getLogsFromLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return logRepository.findAll().stream()
                .filter(log -> log.getTimestamp() != null && log.getTimestamp().isAfter(yesterday))
                .collect(Collectors.toList());
    }
}