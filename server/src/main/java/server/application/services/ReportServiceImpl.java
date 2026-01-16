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
import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.SalesStatsReportDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReportServiceImpl implements ReportService {

    private final LogRepository logRepository;
    private final BranchInventoryItemRepository inventoryRepository;

    public ReportServiceImpl(LogRepository logRepository, BranchInventoryItemRepository inventoryRepository) {
        this.logRepository = logRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public BranchInventoryReportDto getBranchInventoryReport(UUID branchId) {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Branch Inventory Report for Branch ID: " + branchId);

        List<BranchInventoryItem> items = inventoryRepository.findByBranchId(branchId);

        List<BranchInventoryReportDto.InventoryItemDto> itemDtos = items.stream()
            .map(item -> new BranchInventoryReportDto.InventoryItemDto(
                item.getItemId().toString(),
                item.getProductName(),
                item.getCategory(),
                item.getQuantityInStock()
            ))
            .collect(Collectors.toList());

        return new BranchInventoryReportDto(
            LocalDateTime.now().toString(),
            branchId,
            items.size(),
            itemDtos
        );
    }

    @Override
    public SalesStatsReportDto getSalesStatsByBranch() {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Sales Statistics by Branch Report");

        List<LogEntry> purchaseLogs = getPurchaseLogs();
        Map<String, Integer> salesByBranch = new HashMap<>();

        for (LogEntry log : purchaseLogs) {
            String branchId = extractValueFromLog(log.getMessage(), "branchId");
            String quantityStr = extractValueFromLog(log.getMessage(), "quantity");

            if (branchId != null && quantityStr != null) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    salesByBranch.put(branchId, salesByBranch.getOrDefault(branchId, 0) + quantity);
                } catch (NumberFormatException e) {
                }
            }
        }

        return new SalesStatsReportDto(
            LocalDateTime.now().toString(),
            "SALES_BY_BRANCH",
            salesByBranch
        );
    }

    @Override
    public SalesStatsReportDto getSalesStatsByProduct() {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "Admin generated Sales Statistics by Product Report");

        List<LogEntry> purchaseLogs = getPurchaseLogs();
        Map<String, Integer> salesByProduct = new HashMap<>();

        for (LogEntry log : purchaseLogs) {
            String itemId = extractValueFromLog(log.getMessage(), "itemId"); // Assuming logs store itemId or name
            String quantityStr = extractValueFromLog(log.getMessage(), "quantity");

            if (itemId != null && quantityStr != null) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    salesByProduct.put(itemId, salesByProduct.getOrDefault(itemId, 0) + quantity);
                } catch (NumberFormatException e) {
                }
            }
        }

        return new SalesStatsReportDto(
            LocalDateTime.now().toString(),
            "SALES_BY_PRODUCT",
            salesByProduct
        );
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
            return null;
        }
        return null;
    }
}