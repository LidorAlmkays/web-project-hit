package server.application.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import server.application.adaptors.ReportService;
import server.domain.BranchInventoryItem;
import server.domain.LogEntry;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import server.infustructre.adaptors.LogRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReportServiceImpl implements ReportService {

    private final LogRepository logRepository;
    private final BranchInventoryItemRepository inventoryRepository;
    private final Gson gson;

    public ReportServiceImpl(LogRepository logRepository, BranchInventoryItemRepository inventoryRepository) {
        this.logRepository = logRepository;
        this.inventoryRepository = inventoryRepository;
        
        this.gson = new GsonBuilder()
                .setPrettyPrinting() 
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                        new JsonPrimitive(src.toString())) 
                .create();
    }

    @Override
    public String getDailySystemReportJson() {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Daily System Report (Logs)");

        List<LogEntry> todayLogs = getLogsFromLast24Hours();

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("type", "SYSTEM_LOGS");
        reportData.put("totalEvents", todayLogs.size());
        reportData.put("events", todayLogs);

        return gson.toJson(reportData);
    }

    @Override
    public String getBranchInventoryReportJson(UUID branchId) {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Branch Inventory Report for Branch ID: " + branchId);

        List<BranchInventoryItem> items = inventoryRepository.findByBranchId(branchId);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("type", "BRANCH_INVENTORY");
        reportData.put("branchId", branchId);
        reportData.put("totalItems", items.size());
        reportData.put("inventory", items);

        return gson.toJson(reportData);
    }


    public String getSalesStatsByBranchJson() {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Sales Statistics by Branch Report");

        List<LogEntry> purchaseLogs = getPurchaseLogs();
        Map<String, Integer> salesByBranch = new HashMap<>();

        for (LogEntry log : purchaseLogs) {
            String branchId = extractValueFromLog(log.getMessage(), "branchId");
            String quantityStr = extractValueFromLog(log.getMessage(), "quantity");

            if (branchId != null && quantityStr != null) {
                int quantity = Integer.parseInt(quantityStr);
                salesByBranch.put(branchId, salesByBranch.getOrDefault(branchId, 0) + quantity);
            }
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("type", "SALES_BY_BRANCH");
        reportData.put("data", salesByBranch);

        return gson.toJson(reportData);
    }

    public String getSalesStatsByProductJson() {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Sales Statistics by Product Report");

        List<LogEntry> purchaseLogs = getPurchaseLogs();
        Map<String, Integer> salesByProduct = new HashMap<>();

        for (LogEntry log : purchaseLogs) {
            String itemId = extractValueFromLog(log.getMessage(), "itemId");
            String quantityStr = extractValueFromLog(log.getMessage(), "quantity");

            if (itemId != null && quantityStr != null) {
                int quantity = Integer.parseInt(quantityStr);
                salesByProduct.put(itemId, salesByProduct.getOrDefault(itemId, 0) + quantity);
            }
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("type", "SALES_BY_PRODUCT");
        reportData.put("data", salesByProduct);

        return gson.toJson(reportData);
    }

    
    @Override
    public String getDailySystemReportWord() {
        return getDailySystemReportJson(); 
    }

    @Override
    public String getBranchInventoryReportWord(UUID branchId) {
        return getBranchInventoryReportJson(branchId);
    }


    private List<LogEntry> getLogsFromLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return logRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isAfter(yesterday))
                .collect(Collectors.toList());
    }
    
    private List<LogEntry> getPurchaseLogs() {
        return logRepository.findAll().stream()
                .filter(log -> log.getType() == LogEntry.LogType.PURCHASE && log.getMessage().contains("succeeded"))
                .collect(Collectors.toList());
    }

    private String extractValueFromLog(String message, String key) {
        try {
            Pattern pattern = Pattern.compile(key + "=([^,]+)"); 
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
        }
        return null;
    }
}