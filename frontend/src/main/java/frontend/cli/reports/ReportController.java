package frontend.cli.reports;

import frontend.application.services.FrontendReportService;
import frontend.util.ReportFileGenerator;
import shareddto.reporting.SystemReportDto;

import java.time.LocalDate;
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
        System.out.println("\n--- Report Management ---");
        System.out.println("1. Generate Daily System Report (Word/Doc)");
        System.out.println("0. Back");
        
        System.out.print("Select option: ");
        String input = scanner.nextLine();

        if ("1".equals(input)) {
            generateDailyReport();
        }
    }

    private void generateDailyReport() {
        System.out.println("Fetching data from server...");
        try {
            SystemReportDto data = reportService.getSystemReport();
            
            if (data.getEvents().isEmpty()) {
                System.out.println("No logs found for the last 24 hours.");
                return;
            }

            String fileName = "System_Report_" + LocalDate.now() + ".doc";
            
            fileGenerator.generateSystemReportFile(data, fileName);
            
            System.out.println("Done! Open '" + fileName + "' to view the report.");
            
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }
}