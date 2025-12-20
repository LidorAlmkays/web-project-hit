package frontend.console;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class StorageManagementConsole {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private UUID branchId;

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

            System.out.print("Enter Branch ID to manage: ");
            try {
                this.branchId = UUID.fromString(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format. Exiting.");
                return;
            }

            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1": // View Branch Details
                        sendRequest(outputStream, "GET_BRANCH_INFO", String.format("{\"branchId\":\"%s\"}", branchId));
                        break;
                    case "2": // View Inventory
                        sendRequest(outputStream, "GET_INVENTORY_ITEMS", String.format("{\"branchId\":\"%s\"}", branchId));
                        break;
                    case "3": // Restock Existing Item (Update)
                        handleRestockExistingItem(outputStream, scanner);
                        break;
                    case "4": // Add New Item
                        handleAddNewItem(outputStream, scanner);
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
        System.out.println("3. Restock Existing Item");
        System.out.println("4. Add New Item");
        System.out.println("5. Exit");
        System.out.print("Select option: ");
    }

    private void sendRequest(DataOutputStream out, String eventType, String dataJson) throws IOException {
        String jsonMessage = String.format("{\"eventType\":\"%s\", \"data\":%s}", eventType, dataJson);
        out.writeUTF(jsonMessage);
        out.flush();
        System.out.println(">> Sent " + eventType);
    }

    // Maps to UPDATE_INVENTORY_ITEM which calls restockItem on server
    private void handleRestockExistingItem(DataOutputStream out, Scanner scanner) throws IOException {
        System.out.print("Item ID (UUID): ");
        String pid = scanner.nextLine();
        System.out.print("Quantity to add: ");
        int qty = Integer.parseInt(scanner.nextLine());

        // Structure: { branchId: "...", item: { itemId: "...", quantity: ... } }
        String dataJson = String.format("{\"branchId\":\"%s\", \"item\":{\"itemId\":\"%s\", \"quantity\":%d}}", branchId, pid, qty);
        sendRequest(out, "UPDATE_INVENTORY_ITEM", dataJson);
    }

    // Maps to ADD_INVENTORY_ITEM which calls addBranchItem on server
    private void handleAddNewItem(DataOutputStream out, Scanner scanner) throws IOException {
        System.out.print("New Item ID (UUID): "); // Or generate one, but assuming input for now
        String itemId = scanner.nextLine();
        System.out.print("Initial Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());

        // Structure: { branchId: "...", item: { itemId: "...", quantity: ... } }
        String dataJson = String.format("{\"branchId\":\"%s\", \"item\":{\"itemId\":\"%s\", \"quantity\":%d}}", branchId, itemId, qty);
        sendRequest(out, "ADD_INVENTORY_ITEM", dataJson);
    }
}
