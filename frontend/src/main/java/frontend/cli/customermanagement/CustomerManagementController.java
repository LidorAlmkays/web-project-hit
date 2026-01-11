package frontend.cli.customermanagement;

import com.google.gson.Gson;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.customermanagement.request.CustomerCreateRequest;
import shareddto.customermanagement.request.CustomerGetRequest;
import shareddto.customermanagement.response.CustomerDto;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

/**
 * Coordinates user input, API calls, and view rendering for customer management tasks.
 */
public class CustomerManagementController {
    private static final Gson gson = new Gson();
    private final IClientTransport client;
    private final CustomerManagementView view;
    private final Scanner scanner;

    public CustomerManagementController(IClientTransport client, CustomerManagementView view, Scanner scanner) {
        this.client = client;
        this.view = view;
        this.scanner = scanner;
    }

    /**
     * Runs the main CLI loop until the user exits.
     */
    public void run() throws IOException {
        view.header("Task 6 - Customer Management");
        while (true) {
            view.menu();
            if (!scanner.hasNextLine()) {
                view.info("No input. Exiting.");
                return;
            }
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addCustomer();
                    break;
                case "2":
                    getCustomer();
                    break;
                case "3":
                    return;
                default:
                    view.error("Unknown option.");
                    break;
            }
        }
    }

    private void addCustomer() throws IOException {
        view.section("Add Customer");
        String fullName = view.prompt(scanner, "Full name");
        String idNumber = view.prompt(scanner, "ID number");
        String phone = view.prompt(scanner, "Phone");
        String email = view.prompt(scanner, "Email");
        String customerType = promptCustomerType();
        if (customerType == null) {
            return;
        }
        CustomerCreateRequest request = new CustomerCreateRequest(fullName, idNumber, phone, email, customerType);
        SocketMessage response = sendOrReport(EventType.CREATE_CUSTOMER, request, "Add failed: ");
        if (response == null) {
            return;
        }
        view.success("Customer added.");
        view.printCustomer(parseCustomer(response));
    }

    private void getCustomer() throws IOException {
        view.section("Get Customer");
        CustomerGetRequest request = new CustomerGetRequest(view.prompt(scanner, "ID number"));
        SocketMessage response = sendOrReport(EventType.GET_CUSTOMER, request, "Get failed: ");
        if (response == null) {
            return;
        }
        view.printCustomer(parseCustomer(response));
    }

    private String promptCustomerType() {
        while (true) {
            String customerType = view.promptCustomerType(scanner);
            if (customerType.isEmpty()) {
                view.error("Customer type is required.");
                continue;
            }
            String normalized = customerType.toUpperCase(Locale.ROOT);
            if ("NEW".equals(normalized) || "VIP".equals(normalized)) {
                return normalized;
            }
            if ("RETURNING".equals(normalized)) {
                view.error("RETURNING customers are created automatically and cannot be added directly.");
                continue;
            }
            view.error("Invalid type. Use NEW or VIP.");
        }
    }

    /**
     * Sends a request and renders a user-friendly error on failure.
     */
    private SocketMessage sendOrReport(EventType event, Object request, String errorPrefix) throws IOException {
        SocketMessage response = client.send(event, request);
        if (response == null) {
            view.error(errorPrefix + "No response from server.");
            return null;
        }
        Object data = response.getData();
        if (data instanceof String) {
            view.error(errorPrefix + data);
            return null;
        }
        return response;
    }

    private CustomerDto parseCustomer(SocketMessage response) {
        return gson.fromJson(gson.toJsonTree(response.getData()), CustomerDto.class);
    }
}
