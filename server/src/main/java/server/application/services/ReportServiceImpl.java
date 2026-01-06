package server.application.services;

import server.application.adaptors.ReportService;
import server.domain.BranchInventoryItem;
import server.domain.LogEntry;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import server.infustructre.adaptors.LogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportServiceImpl implements ReportService {

    private final LogRepository logRepository;
    private final BranchInventoryItemRepository inventoryRepository;

    public ReportServiceImpl(LogRepository logRepository, BranchInventoryItemRepository inventoryRepository) {
        this.logRepository = logRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // --- דוח 1: לוגים של המערכת (מהיום) ---

    @Override
    public String getDailySystemReportJson() {
        // 1. שולפים את כל הלוגים ומסננים רק את של היום
        List<LogEntry> todayLogs = getLogsFromLast24Hours();

        // 2. בונים JSON ידנית
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"reportDate\": \"").append(LocalDateTime.now()).append("\",\n");
        json.append("  \"totalEvents\": ").append(todayLogs.size()).append(",\n");
        json.append("  \"events\": [\n");

        for (int i = 0; i < todayLogs.size(); i++) {
            LogEntry log = todayLogs.get(i);
            json.append("    {\n");
            json.append("      \"time\": \"").append(log.getTimestamp()).append("\",\n");
            json.append("      \"level\": \"").append(log.getLevel()).append("\",\n");
            json.append("      \"message\": \"").append(escapeJson(log.getMessage())).append("\"\n");
            json.append("    }");
            if (i < todayLogs.size() - 1) json.append(","); // פסיק בין איברים, חוץ מהאחרון
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }

    @Override
    public String getDailySystemReportWord() {
        List<LogEntry> todayLogs = getLogsFromLast24Hours();

        // טריק: בניית HTML פשוט שנפתח ע"י Word
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body>");
        doc.append("<h1 style='color:blue'>Daily System Report</h1>");
        doc.append("<p>Date: <b>").append(LocalDateTime.now().toLocalDate()).append("</b></p>");
        doc.append("<table border='1' cellpadding='5'>");
        doc.append("<tr style='background-color:#eee'><th>Time</th><th>Level</th><th>Message</th></tr>");

        for (LogEntry log : todayLogs) {
            String color = log.getLevel() == LogEntry.LogLevel.ERROR ? "red" : "black";
            doc.append("<tr>");
            doc.append("<td>").append(log.getTimestamp().toLocalTime().toString().substring(0,8)).append("</td>");
            doc.append("<td style='color:").append(color).append("'>").append(log.getLevel()).append("</td>");
            doc.append("<td>").append(log.getMessage()).append("</td>");
            doc.append("</tr>");
        }

        doc.append("</table>");
        doc.append("</body></html>");
        return doc.toString();
    }

    // --- דוח 2: מלאי סניף ---

    @Override
    public String getBranchInventoryReportJson(UUID branchId) {
        List<BranchInventoryItem> items = inventoryRepository.findByBranchId(branchId);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"branchId\": \"").append(branchId).append("\",\n");
        json.append("  \"totalItems\": ").append(items.size()).append(",\n");
        json.append("  \"inventory\": [\n");

        for (int i = 0; i < items.size(); i++) {
            BranchInventoryItem item = items.get(i);
            json.append("    {\n");
            json.append("      \"product\": \"").append(escapeJson(item.getProductName())).append("\",\n");
            json.append("      \"quantity\": ").append(item.getQuantityInStock()).append(",\n");
            json.append("      \"price\": ").append(item.getUnitPrice()).append("\n");
            json.append("    }");
            if (i < items.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}");

        return json.toString();
    }

    @Override
    public String getBranchInventoryReportWord(UUID branchId) {
        List<BranchInventoryItem> items = inventoryRepository.findByBranchId(branchId);

        StringBuilder doc = new StringBuilder();
        doc.append("<html><body>");
        doc.append("<h1>Branch Inventory Report</h1>");
        doc.append("<p>Branch ID: ").append(branchId).append("</p>");
        doc.append("<table border='1' width='100%'>");
        doc.append("<tr style='background-color:#ddd'><th>Product</th><th>Category</th><th>Quantity</th><th>Price</th></tr>");

        for (BranchInventoryItem item : items) {
            doc.append("<tr>");
            doc.append("<td>").append(item.getProductName()).append("</td>");
            doc.append("<td>").append(item.getCategory()).append("</td>");
            // אם המלאי נמוך, נצבע באדום
            String qtyColor = item.getQuantityInStock() < 5 ? "red" : "green";
            doc.append("<td style='color:").append(qtyColor).append("'><b>").append(item.getQuantityInStock()).append("</b></td>");
            doc.append("<td>").append(item.getUnitPrice()).append("</td>");
            doc.append("</tr>");
        }

        doc.append("</table>");
        doc.append("</body></html>");

        return doc.toString();
    }

    // --- פונקציות עזר ---

    private List<LogEntry> getLogsFromLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return logRepository.findAll().stream()
                .filter(log -> log.getTimestamp().isAfter(yesterday))
                .collect(Collectors.toList());
    }

    // פונקציה למניעת שבירת JSON אם יש גרשיים בטקסט
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"").replace("\n", " ");
    }
}