package server.application.adaptors;

import server.domain.customer.Customer;
import java.util.Optional;

public interface CustomerService {
    Optional<Customer> getCustomerByIdNumber(String idNumber);

    Customer addCustomer(String fullName, String idNumber, String phone, String email,
            server.domain.customer.CustomerType customerType);
}
