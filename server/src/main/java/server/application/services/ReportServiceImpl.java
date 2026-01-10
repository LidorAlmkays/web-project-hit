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
        List<LogEntry> todayLogs = getLogsFromLast24Hours();

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportDate", LocalDateTime.now());
        reportData.put("totalEvents", todayLogs.size());
        reportData.put("events", todayLogs);

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
        doc.append("<html><head><style>")
           .append("table {border-collapse: collapse; width: 100%; font-family: Arial, sans-serif;}")
           .append("th, td {border: 1px solid #ddd; padding: 8px; text-align: left;}")
           .append("th {background-color: #f2f2f2;}")
           .append("</style></head><body>");
           
        doc.append("<h1 style='color:darkblue'>Daily System Report</h1>");
        doc.append("<p>Date: <b>").append(LocalDateTime.now().toLocalDate()).append("</b></p>");
        doc.append("<table>");
        
        doc.append("<tr><th>Time</th><th>Level</th><th>Type</th><th>User/ID</th><th>Message</th></tr>");
        
        for (LogEntry log : logs) {
             String color = "black";
             String fontWeight = "normal";

             switch (log.getType()) {
                 case ERROR -> { color = "red"; fontWeight = "bold"; }
                 case AUTHENTICATION -> color = "blue";
                 case PURCHASE -> color = "green";
                 case MANAGEMENT -> color = "#800080"; // Purple
                 case CHAT -> color = "#008080"; // Teal
                 case INFO -> color = "gray";
             }

             doc.append("<tr>");
             doc.append("<td>").append(log.getTimestamp().toLocalTime().toString().split("\\.")[0]).append("</td>"); // חיתוך מילי-שניות
             doc.append("<td>").append(log.getLevel()).append("</td>");
             
             // עיצוב עמודת הסוג
             doc.append("<td style='color:").append(color).append("; font-weight:").append(fontWeight).append("'>")
                .append(log.getType()).append("</td>");
                
             doc.append("<td>").append(log.getEmail()).append("</td>");
             doc.append("<td>").append(log.getMessage()).append("</td>");
             doc.append("</tr>");
        }
        doc.append("</table></body></html>");
        return doc.toString();
    }

    private String generateHtmlForInventory(List<BranchInventoryItem> items, UUID branchId) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><head><style>table {border-collapse: collapse; width: 100%; font-family: Arial;} th, td {border: 1px solid #ddd; padding: 8px;}</style></head><body>");
        doc.append("<h1>Branch Inventory Report</h1>");
        doc.append("<p>Branch ID: ").append(branchId).append("</p>");
        doc.append("<table>");
        doc.append("<tr style='background-color:#eee'><th>Product</th><th>Category</th><th>Quantity</th><th>Price</th></tr>");
        
        for (BranchInventoryItem item : items) {
            String qtyColor = item.getQuantityInStock() < 5 ? "red" : "green";
            String qtyStyle = item.getQuantityInStock() < 5 ? "font-weight:bold; color:red;" : "color:green;";
            
            doc.append("<tr>");
            doc.append("<td>").append(item.getProductName()).append("</td>");
            doc.append("<td>").append(item.getCategory()).append("</td>");
            doc.append("<td style='").append(qtyStyle).append("'>").append(item.getQuantityInStock()).append("</td>");
            doc.append("<td>").append(String.format("%.2f", item.getUnitPrice())).append("</td>");
            doc.append("</tr>");
        }
        doc.append("</table></body></html>");
        return doc.toString();
    }
}