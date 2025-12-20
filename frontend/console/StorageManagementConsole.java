package frontend.console;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class StorageManagementConsole {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        new StorageManagementConsole().start();
    }

    public void start() {
        System.out.println("Starting Storage Management Console...");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);

            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1": // View Branch Details
                        sendRequest(outputStream, "GET_BRANCH_INFO", "{}");
                        break;
                    case "2": // View Inventory
                        sendRequest(outputStream, "GET_INVERTORY_ITEMS", "{}");
                        break;
                    case "3": // Purchase Items
                        handlePurchaseItems(outputStream, scanner);
                        break;
                    case "4": // Restock Items
                        handleRestockItems(outputStream, scanner);
                        break;
                    case "5":
                        System.out.println("Exiting...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
                
                // Optional: Read response from server
                // String response = inputStream.readUTF();
                // System.out.println("Server: " + response);
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("\n--- Storage Management ---");
        System.out.println("1. View Branch Details");
        System.out.println("2. View Inventory");
        System.out.println("3. Purchase Items");
        System.out.println("4. Restock Items");
        System.out.println("5. Exit");
        System.out.print("Select option: ");
    }

    private void sendRequest(DataOutputStream out, String eventType, String dataJson) throws IOException {
        String jsonMessage = String.format("{\"eventType\":\"%s\", \"data\":%s}", eventType, dataJson);
        out.writeUTF(jsonMessage);
        out.flush();
        System.out.println(">> Sent " + eventType);
    }

    private void handlePurchaseItems(DataOutputStream out, Scanner scanner) throws IOException {
        System.out.print("Product ID: ");
        String pid = scanner.nextLine();
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());

        String dataJson = String.format("{\"productId\":\"%s\", \"quantity\":%d, \"action\":\"PURCHASE\"}", pid, qty);
        sendRequest(out, "UPDATE_INVENTORY_ITEM", dataJson);
    }

    private void handleRestockItems(DataOutputStream out, Scanner scanner) throws IOException {
        System.out.print("Product ID: ");
        String pid = scanner.nextLine();
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());

        String dataJson = String.format("{\"productId\":\"%s\", \"quantity\":%d}", pid, qty);
        sendRequest(out, "ADD_INVENTORY_ITEM", dataJson);
    }
}
