package frontend.cli.customermanagement;

import frontend.cli.shared.BaseManagementController;
import frontend.cli.shared.ValidationUtils;
import frontend.transport.IClientTransport;
import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.customermanagement.request.CustomerCreateRequest;
import shareddto.customermanagement.request.CustomerGetRequest;
import shareddto.customermanagement.response.CustomerDto;

import java.io.IOException;
import java.util.Scanner;

public class CustomerManagementController extends BaseManagementController<CustomerManagementView> {

    public CustomerManagementController(IClientTransport client, CustomerManagementView view, Scanner scanner) {
        super(client, view, scanner);
    }

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
        if (!ValidationUtils.isValidIsraeliId(idNumber)) {
            view.error("ID number must be a valid Israeli ID number.");
            return;
        }
        if (!ValidationUtils.isValidPhoneDigits(phone)) {
            view.error("Phone number must contain only digits (9-10 digits).");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            view.error("Invalid email format.");
            return;
        }
        CustomerCreateRequest request = new CustomerCreateRequest(fullName, idNumber, phone, email);
        SocketMessage response = sendOrReport(EventType.CREATE_CUSTOMER, request, "Add failed: ");
        if (response == null) {
            return;
        }
        view.success("Customer added.");
        view.printCustomer(parseCustomer(response));
    }

    private void getCustomer() throws IOException {
        view.section("Get Customer");
        String idNumber = view.prompt(scanner, "ID number");
        if (!ValidationUtils.isValidIsraeliId(idNumber)) {
            view.error("ID number must be a valid Israeli ID number.");
            return;
        }
        CustomerGetRequest request = new CustomerGetRequest(idNumber);
        SocketMessage response = sendOrReport(EventType.GET_CUSTOMER, request, "Get failed: ");
        if (response == null) {
            return;
        }
        view.printCustomer(parseCustomer(response));
    }

    private CustomerDto parseCustomer(SocketMessage response) {
        return gson.fromJson(gson.toJsonTree(response.getData()), CustomerDto.class);
    }
}
