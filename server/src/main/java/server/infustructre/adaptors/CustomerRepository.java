package server.infustructre.adaptors;

import server.domain.customer.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    void save(Customer customer);

    void update(Customer customer);

    void delete(UUID customerId);

    Optional<Customer> findById(UUID customerId);

    Optional<Customer> findByEmail(String email);
}
