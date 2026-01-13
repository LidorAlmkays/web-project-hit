package frontend.util;

import shareddto.reporting.LogEntryDto;
import shareddto.reporting.SystemReportDto;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class ReportFileGenerator {

    public void generateSystemReportFile(SystemReportDto reportData, String fileName) {
        String htmlContent = buildHtmlContent(reportData);
        saveFile(htmlContent, fileName);
    }

    public void generateStatsReportFile(String title, Map<String, Object> reportData, String fileName) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><head><style>")
           .append("body { font-family: Arial, sans-serif; }")
           .append("table { border-collapse: collapse; width: 80%; margin: 20px auto; }")
           .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }")
           .append("th { background-color: #4CAF50; color: white; }")
           .append("tr:nth-child(even){background-color: #f2f2f2;}")
           .append("h1 { text-align: center; color: #333; }")
           .append("</style></head><body>");

        doc.append("<h1>").append(title).append("</h1>");
        
        if (reportData.containsKey("reportDate")) {
            doc.append("<p style='text-align:center'>Report Date: <b>").append(reportData.get("reportDate")).append("</b></p>");
        }

        doc.append("<table>");
        doc.append("<tr><th>Item / Branch ID</th><th>Quantity Sold</th></tr>");

        if (reportData.get("data") instanceof Map) {
            Map<String, Double> dataMap = (Map<String, Double>) reportData.get("data");
            
            if (dataMap.isEmpty()) {
                doc.append("<tr><td colspan='2' style='text-align:center'>No sales data found.</td></tr>");
            } else {
                for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
                    doc.append("<tr>");
                    doc.append("<td>").append(entry.getKey()).append("</td>");
                    doc.append("<td>").append(entry.getValue().intValue()).append("</td>");
                    doc.append("</tr>");
                }
            }
        }
        
        doc.append("</table></body></html>");
        saveFile(doc.toString(), fileName);
    }


    private String buildHtmlContent(SystemReportDto reportData) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><head><style>")
           .append("body { font-family: Arial, sans-serif; }")
           .append("table { border-collapse: collapse; width: 100%; }")
           .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
           .append("th { background-color: #f2f2f2; }")
           .append("</style></head><body>");
           
        doc.append("<h1 style='color:darkblue'>Daily System Report</h1>");
        doc.append("<p>Report Date: <b>").append(reportData.getReportDate()).append("</b></p>");
        doc.append("<p>Total Events: <b>").append(reportData.getTotalEvents()).append("</b></p>");
        
        doc.append("<table>");
        doc.append("<tr><th>Time</th><th>Level</th><th>Type</th><th>Actor</th><th>Message</th></tr>");
        
        if (reportData.getEvents() != null) {
            for (LogEntryDto log : reportData.getEvents()) {
                String color = "black";
                String fontWeight = "normal";

                switch (log.getType()) {
                    case "ERROR" -> { color = "red"; fontWeight = "bold"; }
                    case "AUTHENTICATION" -> color = "blue";
                    case "PURCHASE" -> color = "green";
                    case "MANAGEMENT" -> color = "#800080"; // Purple
                    case "CHAT" -> color = "#008080"; // Teal
                    case "INFO" -> color = "gray";
                }

                doc.append("<tr>");
                String timeDisplay = log.getTimestamp().contains("T") ? 
                                     log.getTimestamp().split("T")[1].split("\\.")[0] : log.getTimestamp();

                doc.append("<td>").append(timeDisplay).append("</td>");
                doc.append("<td>").append(log.getLevel()).append("</td>");
                
                doc.append("<td style='color:").append(color).append("; font-weight:").append(fontWeight).append("'>")
                   .append(log.getType()).append("</td>");
                   
                doc.append("<td>").append(log.getActor()).append("</td>");
                doc.append("<td>").append(log.getMessage()).append("</td>");
                doc.append("</tr>");
            }
        }
        
        doc.append("</table></body></html>");
        return doc.toString();
    }

    private void saveFile(String content, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            System.out.println("Report saved successfully to: " + Paths.get(fileName).toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save report file: " + e.getMessage());
        }
    }
}