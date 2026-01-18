package frontend.cli.reports;

import frontend.services.FrontendChatService;
import frontend.services.FrontendLoggerService;
import frontend.services.FrontendReportService;
import frontend.util.ReportFileGenerator;
import shareddto.reporting.BranchInventoryReportDto;
import shareddto.reporting.SalesStatsReportDto;
import shareddto.reporting.SystemEventLogDto;
import shareddto.reporting.ChatHistoryDto;

import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class ReportController {

    private final FrontendReportService reportService;
    private final FrontendLoggerService loggerService;
    private final FrontendChatService chatService;
    private final ReportFileGenerator fileGenerator;
    private final Scanner scanner;

    public ReportController(FrontendReportService reportService, FrontendLoggerService loggerService, FrontendChatService chatService) {
        this.reportService = reportService;
        this.loggerService = loggerService;
        this.chatService = chatService;
        this.fileGenerator = new ReportFileGenerator();
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n--- System & Reports Management ---");
            System.out.println("1. Export System Logs (Last 24h)");
            System.out.println("2. Generate Branch Inventory Report");
            System.out.println("3. Generate Sales Report (By Branch)");
            System.out.println("4. Generate Sales Report (By Product)");
            System.out.println("5. Export Chat History");
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
                case "5":
                    exportChatHistory();
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
            String fileName = "System_Logs_" + LocalDate.now() + ".doc";
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
            
            String fileName = "Inventory_Branch_" + branchId.toString().substring(0,8) + ".doc";
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
            String fileName = "Sales_By_Branch_" + LocalDate.now() + ".doc";
            fileGenerator.generateSalesReportFile("Sales Report - By Branch", report, fileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void generateSalesByProduct() {
        System.out.println("Generating Sales Report (By Product)...");
        try {
            SalesStatsReportDto report = reportService.getSalesByProductReport();
            String fileName = "Sales_By_Product_" + LocalDate.now() + ".doc";
            fileGenerator.generateSalesReportFile("Sales Report - By Product", report, fileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void exportChatHistory(){
        System.out.println("Exporting Chat History...");
        try {
            ChatHistoryDto report = chatService.fetchChatHistory();
            String fileName = "Chat_History_Report_" + LocalDate.now() + ".doc";
            fileGenerator.generateChatHistoryFile(report, fileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}