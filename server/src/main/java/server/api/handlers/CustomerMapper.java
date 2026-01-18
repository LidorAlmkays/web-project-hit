package server.api.handlers;

import shareddto.customermanagement.response.CustomerDto;
import server.domain.customer.Customer;

public final class CustomerMapper {
    private CustomerMapper() {
    }

    public static CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        String customerId = customer.getCustomerId() != null ? customer.getCustomerId().toString() : null;
        return new CustomerDto(
                customerId,
                customer.getFullName(),
                customer.getIdNumber(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getCustomerType().name(),
                customer.getTotalPurchases(),
                customer.getTotalSpent());
    }

}
