package frontend;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import frontend.transport.SocketClient;
import shareddto.BuyItemRequest;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.GetBranchInfoRequest;
import shareddto.GetInventoryItemsRequest;
import shareddto.UpdateStockRequest;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class StorageManagementConsole {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private UUID branchId;

    public static void main(String[] args) {
        new StorageManagementConsole().start();
    }

    public void start() {
        System.out.println("Starting Storage Management Console...");

        try (SocketClient client = new SocketClient(SERVER_HOST, SERVER_PORT);
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
                try {
                printMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1": // View Branch Details
                        GetBranchInfoRequest branchReq = new GetBranchInfoRequest(branchId.toString());
                        JsonElement branchInfo = sendRequest(client, EventType.GET_BRANCH_INFO, branchReq);
                        printBranchInfo(branchInfo);
                        break;
                    case "2": // View Inventory
                        GetInventoryItemsRequest invReq = new GetInventoryItemsRequest(branchId.toString());
                        JsonElement inventory = sendRequest(client, EventType.GET_INVERTORY_ITEMS, invReq);
                        printInventoryList(inventory);
                        break;
                    case "3": // Restock Existing Item (Update)
                        handleRestockExistingItem(client, scanner);
                        break;
                    case "4": // Buy Item
                        handleBuyItem(client, scanner);
                        break;
                    case "5":
                        System.out.println("Exiting...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
                } catch (Exception e) {
                    System.err.println("Error during operation: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.err.println("Make sure the server is running! (Run '.\\mvnw.cmd -pl server exec:java' in a separate terminal)");
        }
    }

    private void printMenu() {
        System.out.println("\n--- Storage Management ---");
        System.out.println("1. View Branch Details");
        System.out.println("2. View Inventory");
        System.out.println("3. Restock Item");
        System.out.println("4. Buy Item");
        System.out.println("5. Exit");
        System.out.print("Select option: ");
    }

    private JsonElement sendRequest(SocketClient client, EventType eventType, Object data) throws IOException {
        System.out.println(">> Sending " + eventType);
        SocketMessage response = client.send(eventType, data);
        System.out.println("<< Response: " + response.getData());
        return new Gson().toJsonTree(response.getData());
    }

    private void handleRestockExistingItem(SocketClient client, Scanner scanner) throws IOException {
        System.out.print("Item ID (UUID): ");
        String pid = scanner.nextLine();
        System.out.print("Quantity to add: ");
        int qty = Integer.parseInt(scanner.nextLine());

        UpdateStockRequest request = new UpdateStockRequest(branchId.toString(), pid, qty);
        JsonElement response = sendRequest(client, EventType.UPDATE_INVENTORY_ITEM, request);
        if (checkForError(response)) return;
        System.out.println("Update successful:");
        printSingleItem(response);
    }

    private void handleBuyItem(SocketClient client, Scanner scanner) throws IOException {
            System.out.print("Item ID (UUID) to buy: ");
            String pid = scanner.nextLine();
            System.out.print("Customer ID (UUID): ");
            String cid = scanner.nextLine();
            System.out.print("Quantity to buy: ");
            int qty = Integer.parseInt(scanner.nextLine());

            BuyItemRequest request = new BuyItemRequest(branchId.toString(), pid, cid, qty);
            JsonElement response = sendRequest(client, EventType.BUY_INVENTORY_ITEM, request);
            if (checkForError(response)) return;
            System.out.println("Purchase successful:");
            printSingleItem(response);
        }

    private void printBranchInfo(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            System.out.println("No data received.");
            return;
        }
        JsonObject data = json.getAsJsonObject();
        if (data.has("data")) data = data.getAsJsonObject("data");
        
        System.out.println("\n=== Branch Details ===");
        System.out.println("ID:      " + getString(data, "branchId"));
        System.out.println("Name:    " + getString(data, "branchName"));
        System.out.println("Address: " + getString(data, "address"));
        System.out.println("Phone:   " + getString(data, "phoneNumber"));
        System.out.println("Sold:    " + data.get("totalSold"));
        System.out.println("Revenue: " + data.get("totalMoneyEarned"));
        System.out.println("======================\n");
    }

    private void printInventoryList(JsonElement json) {
        if (json == null || json.isJsonNull()) return;
        JsonArray items;
        if (json.isJsonArray()) {
            items = json.getAsJsonArray();
        } else {
            JsonObject root = json.getAsJsonObject();
            items = root.has("data") ? root.getAsJsonArray("data") : new JsonArray();
        }

        System.out.println("\n=== Inventory ===");
        System.out.printf("%-38s | %-30s | %-15s | %-10s | %-8s%n", "Item ID", "Product Name", "Category", "Price", "Stock");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (JsonElement e : items) {
            JsonObject item = e.getAsJsonObject();
            System.out.printf("%-38s | %-30s | %-15s | %-10.2f | %-8d%n",
                    getString(item, "itemId"), getString(item, "productName"), getString(item, "category"),
                    item.get("unitPrice").getAsDouble(), item.get("quantityInStock").getAsInt());
        }
        System.out.println("=================\n");
    }

    private void printSingleItem(JsonElement json) {
        if (json == null || json.isJsonNull()) return;
        JsonObject item = json.getAsJsonObject();
        if (item.has("data")) item = item.getAsJsonObject("data");
        System.out.println("Item: " + getString(item, "productName") + " | New Stock: " + item.get("quantityInStock"));
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "N/A";
    }

    private boolean checkForError(JsonElement json) {
        if (json != null && json.isJsonObject() && json.getAsJsonObject().has("error")) {
            System.out.println("Error: " + json.getAsJsonObject().get("error").getAsString());
            return true;
        }
        return false;
    }
}