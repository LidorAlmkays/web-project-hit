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
            String idNumber = resolveIdNumber(data);
            if (idNumber == null || idNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Customer id number is required");
            }
            Optional<Customer> customer = customerService.getCustomerByIdNumber(idNumber.trim());
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

    private String resolveIdNumber(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof String) {
            return (String) data;
        }
        CustomerGetRequest request = gson.fromJson(gson.toJsonTree(data), CustomerGetRequest.class);
        if (request == null) {
            return null;
        }
        return request.getIdNumber();
    }
}
