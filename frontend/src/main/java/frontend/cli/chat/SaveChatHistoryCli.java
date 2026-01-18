package frontend.cli.chat;

import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.services.FrontendChatService;
import frontend.transport.IClientTransport;
import frontend.util.ReportFileGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

public class SaveChatHistoryCli implements IOptionCli {

    @Override
    public String getOptionName() {
        return "Save Chat History";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) throws IOException {
        System.out.println("\n=== Save Chat History ===");
        System.out.println("Fetching chat history from server...");
        
        try {
            FrontendChatService chatService = new FrontendChatService(client);
            var chatHistory = chatService.fetchChatHistory();
            
            String fileName = "Chat_History_" + LocalDate.now() + ".doc";
            ReportFileGenerator fileGenerator = new ReportFileGenerator();
            fileGenerator.generateChatHistoryFile(chatHistory, fileName);
            
            System.out.println("Chat history saved successfully!");
            System.out.println("File: " + fileName);
            
        } catch (Exception e) {
            System.err.println("Error saving chat history: " + e.getMessage());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
        
        return CliResult.BACK;
    }
}
