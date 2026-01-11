package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.customermanagement.request.CustomerCreateRequest;
import server.application.adaptors.CustomerService;
import server.domain.customer.Customer;
import server.domain.customer.CustomerType;

import java.net.Socket;
import java.util.Locale;

public class CreateCustomerHandler extends AbstractSocketHandler {
    private final CustomerService customerService;

    public CreateCustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            CustomerCreateRequest request = gson.fromJson(gson.toJsonTree(data), CustomerCreateRequest.class);
            CustomerType customerType = parseCustomerType(request.getCustomerType());
            ensureCreatableType(customerType);

            Customer customer = customerService.addCustomer(
                    request.getFullName(),
                    request.getIdNumber(),
                    request.getPhone(),
                    request.getEmail(),
                    customerType);

            sendMessage(clientSocket,
                    new SocketMessage(EventType.CREATE_CUSTOMER, CustomerMapper.toDto(customer)));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.CREATE_CUSTOMER, e.getMessage()));
        }
    }

    private CustomerType parseCustomerType(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("customerType is required");
        }
        return CustomerType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private void ensureCreatableType(CustomerType customerType) {
        if (customerType == CustomerType.RETURNING) {
            throw new IllegalArgumentException("customerType RETURNING cannot be added directly");
        }
    }
}
