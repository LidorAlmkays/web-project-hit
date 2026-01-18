package frontend.util;

import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.ChatHistoryDto;
import shareddto.reporting.ChatHistoryEntryDto;
import shareddto.reporting.ChatSessionDto;
import shareddto.reporting.LogEntryDto;
import shareddto.reporting.SalesStatsReportDto;
import shareddto.reporting.SystemEventLogDto;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class ReportFileGenerator {

    // --- 1. System Logs Generator ---
    public void generateLogDumpFile(SystemEventLogDto logData, String fileName) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><head><style>")
           .append("body { font-family: Arial, sans-serif; }")
           .append("table { border-collapse: collapse; width: 100%; font-size: 12px; }")
           .append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; }")
           .append("th { background-color: #f2f2f2; }")
           .append(".error { color: red; font-weight: bold; }")
           .append(".info { color: gray; }")
           .append("</style></head><body>");
           
        doc.append("<h1 style='color:darkblue'>System Event Logs</h1>");
        doc.append("<p>Generated Date: <b>").append(logData.getGeneratedDate()).append("</b></p>");
        doc.append("<p>Total Entries: <b>").append(logData.getTotalEntries()).append("</b></p>");
        
        doc.append("<table>");
        doc.append("<tr><th>Time</th><th>Level</th><th>Type</th><th>Actor</th><th>Message</th></tr>");
        
        if (logData.getLogs() != null) {
            for (LogEntryDto log : logData.getLogs()) {
                String rowClass = log.getLevel().equals("ERROR") ? "class='error'" : "";
                doc.append("<tr ").append(rowClass).append(">");
                
                String timeDisplay = log.getTimestamp().replace("T", " ");
                doc.append("<td>").append(timeDisplay).append("</td>");
                doc.append("<td>").append(log.getLevel()).append("</td>");
                doc.append("<td>").append(log.getType()).append("</td>");
                doc.append("<td>").append(log.getActor()).append("</td>");
                doc.append("<td>").append(log.getMessage()).append("</td>");
                doc.append("</tr>");
            }
        }
        doc.append("</table></body></html>");
        saveFile(doc.toString(), fileName);
    }

    // --- 2. Inventory Report Generator ---
    public void generateInventoryReportFile(BranchInventoryReportDto reportData, String fileName) {
        StringBuilder doc = new StringBuilder();
        addHeader(doc, "Branch Inventory Report");
        
        doc.append("<p>Branch ID: <b>").append(reportData.getBranchId()).append("</b></p>");
        doc.append("<p>Total Unique Items: <b>").append(reportData.getTotalUniqueItems()).append("</b></p>");

        doc.append("<table>");
        doc.append("<tr><th>Item Name</th><th>Category</th><th>Quantity in Stock</th></tr>");

        if (reportData.getItems() != null) {
            for (BranchInventoryReportDto.InventoryItemDto item : reportData.getItems()) {
                doc.append("<tr>");
                doc.append("<td>").append(item.getName()).append("</td>");
                doc.append("<td>").append(item.getCategory()).append("</td>");
                doc.append("<td>").append(item.getQuantity()).append("</td>");
                doc.append("</tr>");
            }
        }
        doc.append("</table></body></html>");
        saveFile(doc.toString(), fileName);
    }

    // --- 3. Sales Stats Generator ---
    public void generateSalesReportFile(String title, SalesStatsReportDto reportData, String fileName) {
        StringBuilder doc = new StringBuilder();
        addHeader(doc, title);

        doc.append("<p>Aggregation Type: <b>").append(reportData.getAggregationType()).append("</b></p>");

        doc.append("<table>");
        doc.append("<tr><th>Identifier (Branch/Product)</th><th>Quantity Sold</th></tr>");

        if (reportData.getSalesData() != null && !reportData.getSalesData().isEmpty()) {
            for (Map.Entry<String, Integer> entry : reportData.getSalesData().entrySet()) {
                doc.append("<tr>");
                doc.append("<td>").append(entry.getKey()).append("</td>");
                doc.append("<td>").append(entry.getValue()).append("</td>");
                doc.append("</tr>");
            }
        } else {
            doc.append("<tr><td colspan='2' style='text-align:center'>No sales data found.</td></tr>");
        }
        
        doc.append("</table></body></html>");
        saveFile(doc.toString(), fileName);
    }

    private void addHeader(StringBuilder doc, String title) {
        doc.append("<html><head><style>")
           .append("body { font-family: Arial, sans-serif; }")
           .append("table { border-collapse: collapse; width: 80%; margin: 20px auto; }")
           .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }")
           .append("th { background-color: #4CAF50; color: white; }")
           .append("tr:nth-child(even){background-color: #f2f2f2;}")
           .append("h1 { text-align: center; color: #333; }")
           .append("</style></head><body>");
        doc.append("<h1>").append(title).append("</h1>");
    }

    // --- 4. Chat History Generator ---
    public void generateChatHistoryFile(ChatHistoryDto chatData, String fileName) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><head><style>")
           .append("body { font-family: Arial, sans-serif; }")
           .append(".chat-session { margin: 20px 0; padding: 15px; border: 2px solid #4CAF50; border-radius: 5px; }")
           .append(".session-header { background-color: #4CAF50; color: white; padding: 10px; margin: -15px -15px 10px -15px; border-radius: 3px 3px 0 0; }")
           .append("table { border-collapse: collapse; width: 100%; font-size: 12px; margin-top: 10px; }")
           .append("th, td { border: 1px solid #ddd; padding: 6px; text-align: left; }")
           .append("th { background-color: #f2f2f2; }")
           .append("tr:nth-child(even){background-color: #f9f9f9;}")
           .append("h1 { color: darkblue; }")
           .append(".participants { margin: 10px 0; font-weight: bold; color: #333; }")
           .append("</style></head><body>");
           
        doc.append("<h1>Chat History Report</h1>");
        doc.append("<p>Generated Date: <b>").append(chatData.getGeneratedDate()).append("</b></p>");
        doc.append("<p>Total Chat Sessions: <b>").append(chatData.getTotalChats()).append("</b></p>");
        
        if (chatData.getChatSessions() != null && !chatData.getChatSessions().isEmpty()) {
            for (ChatSessionDto session : chatData.getChatSessions()) {
                doc.append("<div class='chat-session'>");
                doc.append("<div class='session-header'>");
                doc.append("<strong>").append(session.getFileName()).append("</strong>");
                doc.append("</div>");
                
                doc.append("<div class='participants'>");
                doc.append("Participants: ").append(String.join(", ", session.getParticipants()));
                doc.append("</div>");
                
                doc.append("<p><strong>Started:</strong> ").append(session.getStartTime()).append("</p>");
                doc.append("<p><strong>Ended:</strong> ").append(session.getEndTime()).append("</p>");
                
                if (session.getMessages() != null && !session.getMessages().isEmpty()) {
                    doc.append("<table>");
                    doc.append("<tr><th>Time</th><th>Sender</th><th>Message</th></tr>");
                    
                    for (ChatHistoryEntryDto message : session.getMessages()) {
                        doc.append("<tr>");
                        doc.append("<td>").append(message.getTimestamp()).append("</td>");
                        doc.append("<td>").append(message.getSenderEmail()).append("</td>");
                        doc.append("<td>").append(escapeHtml(message.getMessage())).append("</td>");
                        doc.append("</tr>");
                    }
                    
                    doc.append("</table>");
                } else {
                    doc.append("<p><em>No messages in this chat session.</em></p>");
                }
                
                doc.append("</div>");
            }
        } else {
            doc.append("<p><em>No chat history found.</em></p>");
        }
        
        doc.append("</body></html>");
        saveFile(doc.toString(), fileName);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void saveFile(String content, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            System.out.println("File saved successfully: " + Paths.get(fileName).toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save file: " + e.getMessage());
        }
    }
}