package frontend.cli.reports;

import frontend.services.FrontendLoggerService;
import frontend.services.FrontendReportService;
import frontend.util.ReportFileGenerator;
import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.SalesStatsReportDto;
import shareddto.reporting.SystemEventLogDto;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class ReportController {

    private final FrontendReportService reportService;
    private final FrontendLoggerService loggerService;
    private final ReportFileGenerator fileGenerator;
    private final Scanner scanner;

    public ReportController(FrontendReportService reportService, FrontendLoggerService loggerService) {
        this.reportService = reportService;
        this.loggerService = loggerService;
        this.fileGenerator = new ReportFileGenerator();
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n--- System & Reports Management ---");
            System.out.println("1. Export System Logs (Last 24h)"); // שם חדש ומדויק
            System.out.println("2. Generate Branch Inventory Report"); // חדש
            System.out.println("3. Generate Sales Report (By Branch)");
            System.out.println("4. Generate Sales Report (By Product)");
            System.out.println("0. Back");
            
            System.out.print("Select option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    exportSystemLogs();
                    break;
                case "2":
                    generateBranchInventoryReport();
                    break;
                case "3":
                    generateSalesByBranch();
                    break;
                case "4":
                    generateSalesByProduct();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void exportSystemLogs() {
        System.out.println("Fetching system logs...");
        try {
            SystemEventLogDto logs = loggerService.fetchSystemLogs();
            String fileName = "System_Logs_" + LocalDate.now() + ".html"; // שיניתי ל-html כי זה הפורמט ש-Generator מייצר
            fileGenerator.generateLogDumpFile(logs, fileName);
        } catch (Exception e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
    }

    private void generateBranchInventoryReport() {
        System.out.print("Enter Branch ID (UUID): ");
        String idStr = scanner.nextLine();
        try {
            UUID branchId = UUID.fromString(idStr);
            System.out.println("Fetching inventory data...");
            BranchInventoryReportDto report = reportService.getBranchInventoryReport(branchId);
            
            String fileName = "Inventory_Branch_" + branchId.toString().substring(0,8) + ".html";
            fileGenerator.generateInventoryReportFile(report, fileName);
            
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void generateSalesByBranch() {
        System.out.println("Generating Sales Report (By Branch)...");
        try {
            SalesStatsReportDto report = reportService.getSalesByBranchReport();
            String fileName = "Sales_By_Branch_" + LocalDate.now() + ".html";
            fileGenerator.generateSalesReportFile("Sales Report - By Branch", report, fileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void generateSalesByProduct() {
        System.out.println("Generating Sales Report (By Product)...");
        try {
            SalesStatsReportDto report = reportService.getSalesByProductReport();
            String fileName = "Sales_By_Product_" + LocalDate.now() + ".html";
            fileGenerator.generateSalesReportFile("Sales Report - By Product", report, fileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}