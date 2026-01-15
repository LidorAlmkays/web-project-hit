package server.api.handlers;

import java.net.Socket;
import java.util.Optional;

import server.application.adaptors.CustomerService;
import server.domain.customer.Customer;
import shareddto.EventType;
import shareddto.SocketMessage;

public class GetCustomerHandler extends AbstractSocketHandler {
    private final CustomerService customerService;

    public GetCustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            String idNumber = gson.fromJson(gson.toJson(data), String.class);
            Optional<Customer> customer = customerService.getCustomerByIdNumber(idNumber);

            if (customer.isEmpty()) {
                throw new IllegalArgumentException("Customer not found");
            }

            sendSuccess(clientSocket, customer.get());
        } catch (IllegalArgumentException e) {
            sendError(clientSocket, e.getMessage());
        } catch (Exception e) {
            sendError(clientSocket, "Internal server error: " + e.getMessage());
        }
    }

    private void sendSuccess(Socket clientSocket, Object payload) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.GET_CUSTOMER, payload));
    }

    private void sendError(Socket clientSocket, String message) throws Exception {
        sendMessage(clientSocket, new SocketMessage(EventType.GET_CUSTOMER, message));
    }
}