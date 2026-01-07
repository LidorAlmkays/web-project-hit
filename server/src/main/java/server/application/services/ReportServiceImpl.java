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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        // 1. שולפים את הנתונים
        List<LogEntry> todayLogs = getLogsFromLast24Hours();

        // 2. יוצרים מפה (Map) שתייצג את מבנה ה-JSON שאנחנו רוצים
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("totalEvents", todayLogs.size());
        reportData.put("events", todayLogs); // Gson יודע להפוך את הרשימה הזו למערך JSON לבד!

        // 3. המרה אוטומטית למחרוזת
        return gson.toJson(reportData);
    }


    @Override
    public String getBranchInventoryReportJson(UUID branchId) {
        List<BranchInventoryItem> items = inventoryRepository.findByBranchId(branchId);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("branchId", branchId);
        reportData.put("totalItems", items.size());
        reportData.put("inventory", items);

        return gson.toJson(reportData);
    }


    @Override
    public String getDailySystemReportWord() {
        return generateHtmlForLogs(getLogsFromLast24Hours());
    }

    @Override
    public String getBranchInventoryReportWord(UUID branchId) {
        return generateHtmlForInventory(inventoryRepository.findByBranchId(branchId), branchId);
    }

    
    private List<LogEntry> getLogsFromLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return logRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isAfter(yesterday))
                .collect(Collectors.toList());
    }
    
    private String generateHtmlForLogs(List<LogEntry> logs) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body>");
        doc.append("<h1 style='color:blue'>Daily System Report</h1>");
        doc.append("<p>Date: <b>").append(LocalDateTime.now().toLocalDate()).append("</b></p>");
        doc.append("<table border='1' cellpadding='5'>");
        doc.append("<tr style='background-color:#eee'><th>Time</th><th>Level</th><th>Message</th></tr>");
        for (LogEntry log : logs) {
             String color = log.getLevel().name().equals("ERROR") ? "red" : "black";
             doc.append("<tr>");
             doc.append("<td>").append(log.getTimestamp().toLocalTime().toString().substring(0,8)).append("</td>");
             doc.append("<td style='color:").append(color).append("'>").append(log.getLevel()).append("</td>");
             doc.append("<td>").append(log.getMessage()).append("</td>");
             doc.append("</tr>");
        }
        doc.append("</table></body></html>");
        return doc.toString();
    }

    private String generateHtmlForInventory(List<BranchInventoryItem> items, UUID branchId) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body>");
        doc.append("<h1>Branch Inventory Report</h1>");
        doc.append("<p>Branch ID: ").append(branchId).append("</p>");
        doc.append("<table border='1' width='100%'>");
        doc.append("<tr style='background-color:#ddd'><th>Product</th><th>Category</th><th>Quantity</th><th>Price</th></tr>");
        for (BranchInventoryItem item : items) {
            String qtyColor = item.getQuantityInStock() < 5 ? "red" : "green";
            doc.append("<tr>");
            doc.append("<td>").append(item.getProductName()).append("</td>");
            doc.append("<td>").append(item.getCategory()).append("</td>");
            doc.append("<td style='color:").append(qtyColor).append("'><b>").append(item.getQuantityInStock()).append("</b></td>");
            doc.append("<td>").append(item.getUnitPrice()).append("</td>");
            doc.append("</tr>");
        }
        doc.append("</table></body></html>");
        return doc.toString();
    }
}