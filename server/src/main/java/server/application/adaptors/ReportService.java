package server.application.adaptors;

import shareddto.reporting.SystemReportDto;;
import java.util.UUID;

public interface ReportService {
    SystemReportDto getSystemReportData();
    
    // דרישה: דוח מלאי לסניף (JSON)
    String getBranchInventoryReportJson(UUID branchId);
    
    // דרישה: דוח מלאי לסניף (Word)
    String getBranchInventoryReportWord(UUID branchId);
}