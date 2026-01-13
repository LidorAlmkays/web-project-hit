package server.application.adaptors;

import java.util.UUID;

public interface ReportService {
    // דוחות קיימים
    String getDailySystemReportJson();
    String getDailySystemReportWord();
    
    String getBranchInventoryReportJson(UUID branchId);
    String getBranchInventoryReportWord(UUID branchId);

    String getSalesStatsByBranchJson();
    String getSalesStatsByProductJson();
}