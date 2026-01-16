package server.application.adaptors;

import java.util.UUID;

import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.SalesStatsReportDto;

public interface ReportService {
    // --- Business Reports  ---
    
    BranchInventoryReportDto getBranchInventoryReport(UUID branchId);
    SalesStatsReportDto getSalesStatsByBranch();
    SalesStatsReportDto getSalesStatsByProduct();
}