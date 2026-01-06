package server.application.adaptors;

import java.util.UUID;

public interface ReportService {
    String getDailySystemReportJson();
    String getDailySystemReportWord();

    // דרישה: דוח מלאי לסניף (JSON)
    String getBranchInventoryReportJson(UUID branchId);
    
    // דרישה: דוח מלאי לסניף (Word)
    String getBranchInventoryReportWord(UUID branchId);
}