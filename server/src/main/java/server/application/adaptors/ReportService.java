package server.application.adaptors;

import java.util.UUID;

public interface ReportService {
    // --- Business Reports Only ---
    
    // Returns JSON of BranchInventoryReportDto
    String getBranchInventoryReportJson(UUID branchId);
    
    // Returns JSON of SalesStatsReportDto
    String getSalesStatsByBranchJson();
    String getSalesStatsByProductJson();
}