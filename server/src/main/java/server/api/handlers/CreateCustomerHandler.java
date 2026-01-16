package server.api.handlers;

import shareddto.EventType;
import shareddto.SocketMessage;
import shareddto.customermanagement.request.CustomerCreateRequest;
import server.application.adaptors.CustomerService;
import server.domain.customer.Customer;

import java.net.Socket;

public class CreateCustomerHandler extends AbstractSocketHandler {
    private final CustomerService customerService;

    public CreateCustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void handle(Object data, Socket clientSocket) throws Exception {
        try {
            CustomerCreateRequest request = gson.fromJson(gson.toJsonTree(data), CustomerCreateRequest.class);

            Customer customer = customerService.addCustomer(
                    request.getFullName(),
                    request.getIdNumber(),
                    request.getPhone(),
                    request.getEmail());

            sendMessage(clientSocket,
                    new SocketMessage(EventType.CREATE_CUSTOMER, CustomerMapper.toDto(customer)));
        } catch (Exception e) {
            sendMessage(clientSocket, new SocketMessage(EventType.CREATE_CUSTOMER, e.getMessage()));
        }
    }
}
