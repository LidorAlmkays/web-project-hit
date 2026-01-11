package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.customermanagement.request.CustomerGetRequest;
import server.application.adaptors.CustomerService;
import server.domain.customer.Customer;

import java.net.Socket;
import java.util.Optional;
public class GetCustomerHandler extends AbstractSocketHandler {
    private final CustomerService customerService;

    public GetCustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            CustomerGetRequest request = gson.fromJson(gson.toJsonTree(data), CustomerGetRequest.class);
            Optional<Customer> customer = resolveCustomer(request);
            if (customer.isEmpty()) {
                throw new IllegalArgumentException("Customer not found");
            }
            sendMessage(clientSocket,
                    new SocketMessage(EventType.GET_CUSTOMER, CustomerMapper.toDto(customer.get())));
        } catch (IllegalArgumentException e) {
            sendMessage(clientSocket, new SocketMessage(EventType.GET_CUSTOMER, e.getMessage()));
        } catch (Exception e) {
            sendMessage(clientSocket,
                    new SocketMessage(EventType.GET_CUSTOMER, "Internal server error: " + e.getMessage()));
        }
    }

    private Optional<Customer> resolveCustomer(CustomerGetRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        if (request.getIdNumber() != null && !request.getIdNumber().trim().isEmpty()) {
            return customerService.getCustomerByIdNumber(request.getIdNumber().trim());
        }
        return Optional.empty();
    }
}
