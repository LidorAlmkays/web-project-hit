package frontend.cli.reports;

import frontend.application.services.FrontendReportService;
import frontend.util.ReportFileGenerator;
import shareddto.reporting.SystemReportDto;

import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;

public class ReportController {

    private final FrontendReportService reportService;
    private final ReportFileGenerator fileGenerator;
    private final Scanner scanner;

    public ReportController(FrontendReportService reportService) {
        this.reportService = reportService;
        this.fileGenerator = new ReportFileGenerator();
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n--- Report Management (Admin) ---");
            System.out.println("1. Generate Daily System Report (Logs)");
            System.out.println("2. Generate Sales Report by Branch"); 
            System.out.println("3. Generate Sales Report by Product");
            System.out.println("0. Back");
            
            System.out.print("Select option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    generateDailyReport();
                    break;
                case "2":
                    generateSalesByBranchReport();
                    break;
                case "3":
                    generateSalesByProductReport();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void generateDailyReport() {
        System.out.println("Fetching log data from server...");
        try {
            SystemReportDto data = reportService.getSystemReport();
            String fileName = "System_Report_" + LocalDate.now() + ".doc";
            fileGenerator.generateSystemReportFile(data, fileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    private void generateSalesByBranchReport() {
        System.out.println("Generating Sales by Branch Report...");
        try {
            Map<String, Object> data = reportService.getSalesByBranchReport();
            String fileName = "Sales_By_Branch_" + LocalDate.now() + ".doc";
            fileGenerator.generateStatsReportFile("Sales Report - By Branch", data, fileName);
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    private void generateSalesByProductReport() {
        System.out.println("Generating Sales by Product Report...");
        try {
            Map<String, Object> data = reportService.getSalesByProductReport();
            String fileName = "Sales_By_Product_" + LocalDate.now() + ".doc";
            fileGenerator.generateStatsReportFile("Sales Report - By Product", data, fileName);
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }
}