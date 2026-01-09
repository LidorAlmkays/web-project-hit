package server.application.services;

import server.application.adaptors.CustomerService;
import server.domain.customer.Customer;
import server.domain.customer.CustomerType;
import server.infustructre.adaptors.CustomerRepository;
import server.infustructre.adaptors.LogRepository;

import java.util.Optional;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LogRepository logRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, LogRepository logRepository) {
        this.customerRepository = customerRepository;
        this.logRepository = logRepository;
    }

    @Override
    public Optional<Customer> getCustomerByIdNumber(String idNumber) {
        logRepository.info("Getting customer by idNumber (ת.ז): " + idNumber);

        if (idNumber == null || idNumber.trim().isEmpty()) {
            Error error = new Error("Get customer by idNumber failed, idNumber is null or empty");
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Customer> customer = customerRepository.findByIdNumber(idNumber);
            if (customer.isPresent()) {
                logRepository.info(LogEntry.LogType.CUSTOMER_MANAGEMENT, "Customer found by idNumber, idNumber=" + idNumber
                        + ", customerId=" + customer.get().getCustomerId());
            } else {
                logRepository.info(LogEntry.LogType.CUSTOMER_MANAGEMENT, "Customer not found by idNumber, idNumber=" + idNumber);
            }
            return customer;
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            Error error = new Error("Get customer by idNumber error, idNumber=" + idNumber
                    + ", message=" + e.getMessage());
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new RuntimeException(error);
        }
    }

    @Override
    public Customer addCustomer(String fullName, String idNumber, String phone, String email) {
        logRepository.info("Adding new customer, fullName=" + fullName + ", idNumber=" + idNumber
                + ", phone=" + phone + ", email=" + email);

        if (fullName == null || fullName.trim().isEmpty()) {
            Error error = new Error("Add customer failed, fullName must not be null or empty");
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (idNumber == null || idNumber.trim().isEmpty()) {
            Error error = new Error("Add customer failed, idNumber (ת.ז) must not be null or empty");
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (phone == null || phone.trim().isEmpty()) {
            Error error = new Error("Add customer failed, phone must not be null or empty");
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (email == null || email.trim().isEmpty()) {
            Error error = new Error("Add customer failed, email must not be null or empty");
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Customer> existingByIdNumber = customerRepository.findByIdNumber(idNumber);
            if (existingByIdNumber.isPresent()) {
                Error error = new Error(
                        "Add customer failed, customer with idNumber (ת.ז) already exists: " + idNumber);
                logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            Customer newCustomer = new Customer(fullName, idNumber, phone, email, CustomerType.NEW);

            customerRepository.save(newCustomer);

            logRepository.info("Add customer succeeded, customerId=" + newCustomer.getCustomerId()
                    + ", fullName=" + fullName + ", idNumber=" + idNumber + ", email=" + email
                    + ", customerType=" + CustomerType.NEW);

            return newCustomer;
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            Error error = new Error("Add customer error, fullName=" + fullName + ", idNumber=" + idNumber
                    + ", message=" + e.getMessage());
            logRepository.error(LogEntry.LogType.CUSTOMER_MANAGEMENT, error.getMessage());
            throw new RuntimeException(error);
        }
    }
}
