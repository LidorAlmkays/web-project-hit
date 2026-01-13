package frontend.cli.storagemanagement;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import frontend.cli.CliResult;
import frontend.cli.IOptionCli;
import frontend.transport.IClientTransport;
import shareddto.AddItemRequest;
import shareddto.BuyItemRequest;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.GetBranchInfoRequest;
import shareddto.GetInventoryItemsRequest;
import shareddto.UpdateStockRequest;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class StorageManagementConsole implements IOptionCli {
    
    private UUID branchId;
    
    @Override
    public String getOptionName() {
        return "Storage Management";
    }

    @Override
    public CliResult run(IClientTransport client, Scanner scanner) {
        System.out.println("Starting Storage Management Console...");

        // Note: client and scanner are passed from App.java

            System.out.print("Enter Branch ID to manage: ");
            try {
                this.branchId = UUID.fromString(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid Branch UUID format. Exiting.");
                return CliResult.BACK;
            }

            boolean running = true;
            while (running) {
                try {
                    printMenu();
                    String choice = scanner.nextLine();

                    switch (choice) {
                        case "1":
                            printBranchInfo(sendRequest(client, EventType.GET_BRANCH_INFO, new GetBranchInfoRequest(branchId.toString())));
                            break;
                        case "2":
                            printInventoryList(sendRequest(client, EventType.GET_INVERTORY_ITEMS, new GetInventoryItemsRequest(branchId.toString())));
                            break;
                        case "3":
                            handleRestockExistingItem(client, scanner);
                            break;
                        case "4":
                            handleBuyItem(client, scanner);
                            break;
                        case "5":
                            handleAddItem(client, scanner);
                            break;
                        case "6":
                            System.out.println("Exiting...");
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid option.");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

        return CliResult.BACK;
    }

    private void printMenu() {
        System.out.println("\n--- Storage Management ---");
        System.out.println("1. View Branch Details");
        System.out.println("2. View Inventory");
        System.out.println("3. Restock Item");
        System.out.println("4. Buy Item");
        System.out.println("5. Add New Item");
        System.out.println("6. Exit");
        System.out.print("Select option: ");
    }

    private JsonElement sendRequest(IClientTransport client, EventType eventType, Object data) throws IOException {
        SocketMessage response = client.send(eventType, data);
        return new Gson().toJsonTree(response.getData());
    }

    private void handleRestockExistingItem(IClientTransport client, Scanner scanner) throws IOException {
        System.out.print("Item ID (UUID): ");
        String pidInput = scanner.nextLine();
        
        // Manual validation for clearer error message
        try {
            UUID.fromString(pidInput); 
        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: '" + pidInput + "' is not a valid Item UUID format.");
            return;
        }

        System.out.print("Quantity to add: ");
        String qtyInput = scanner.nextLine();
        int qty;
        try {
            qty = Integer.parseInt(qtyInput);
        } catch (NumberFormatException e) {
            System.err.println("Validation Error: Quantity must be a number.");
            return;
        }

        UpdateStockRequest request = new UpdateStockRequest(branchId.toString(), pidInput, qty);
        JsonElement response = sendRequest(client, EventType.UPDATE_INVENTORY_ITEM, request);
        if (checkForError(response)) return;
        System.out.println("Update successful:");
        printSingleItem(response);
    }

    private void handleBuyItem(IClientTransport client, Scanner scanner) throws IOException {
        System.out.print("Item ID (UUID) to buy: ");
        String pidInput = scanner.nextLine();
        
        try {
            UUID.fromString(pidInput);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: '" + pidInput + "' is not a valid Item UUID format.");
            return;
        }

        System.out.print("Customer ID (UUID): ");
        String cid = scanner.nextLine();
        
        try {
            UUID.fromString(cid);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: '" + cid + "' is not a valid Customer UUID format.");
            return;
        }

        System.out.print("Quantity to buy: ");
        int qty = Integer.parseInt(scanner.nextLine());

        BuyItemRequest request = new BuyItemRequest(branchId.toString(), pidInput, cid, qty);
        JsonElement response = sendRequest(client, EventType.BUY_INVENTORY_ITEM, request);
        if (checkForError(response)) return;
        System.out.println("Purchase successful:");
        printSingleItem(response);
    }

    private void handleAddItem(IClientTransport client, Scanner scanner) throws IOException {
        System.out.print("Product Name: ");
        String name = scanner.nextLine();

        System.out.print("Category: ");
        String category = scanner.nextLine();

        System.out.print("Unit Price: ");
        double price;
        try {
            price = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Validation Error: Price must be a number.");
            return;
        }

        System.out.print("Initial Quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Validation Error: Quantity must be a number.");
            return;
        }

        AddItemRequest request = new AddItemRequest(branchId.toString(), name, category, price, qty);
        JsonElement response = sendRequest(client, EventType.ADD_INVENTORY_ITEM, request);
        if (checkForError(response)) return;
        System.out.println("Item added successfully:");
        printSingleItem(response);
    }

    private void printBranchInfo(JsonElement json) {
        if (json == null || json.isJsonNull()) return;
        JsonObject data = json.getAsJsonObject();
        if (data.has("data")) data = data.getAsJsonObject("data");
        
        System.out.println("\n=== Branch Details ===");
        System.out.println("ID:      " + getString(data, "branchId"));
        System.out.println("Name:    " + getString(data, "branchName"));
        System.out.println("Address: " + getString(data, "address"));
        System.out.println("Phone:   " + getString(data, "phoneNumber"));
        System.out.println("Sold:    " + data.get("totalSold"));
        System.out.println("Revenue: " + data.get("totalMoneyEarned"));
        System.out.println("======================");
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

        String rowFormat = "%-38s | %-25s | %-15s | %-10s | %-8s%n";

        System.out.println("\n=== Current Inventory ===");
        
        System.out.printf(rowFormat, "Item ID", "Product Name", "Category", "Price", "Stock");
        System.out.println("-".repeat(105)); 

        for (JsonElement e : items) {
            JsonObject item = e.getAsJsonObject();
            
            String name = getString(item, "productName");
            if (name.length() > 25) {
                name = name.substring(0, 22) + "...";
            }

            System.out.printf(rowFormat,
                    getString(item, "itemId"), 
                    name, 
                    getString(item, "category"),
                    String.format("%.2f", item.get("unitPrice").getAsDouble()), 
                    item.get("quantityInStock").getAsInt());
        }
        System.out.println("=========================\n");
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
        if (json == null || json.isJsonNull()) {
            return false;
        }
        // Handle structured error: {"error": "message"}
        if (json.isJsonObject() && json.getAsJsonObject().has("error")) {
            JsonElement errorElement = json.getAsJsonObject().get("error");
            System.out.println("Error: " + (errorElement.isJsonNull() ? "Unknown error" : errorElement.getAsString()));
            return true;
        }
        // Handle cases where the server sends a plain string as an error
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            System.out.println("Error: " + json.getAsString());
            return true;
        }
        return false;
    }
}