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
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Customer> customer = customerRepository.findByIdNumber(idNumber);
            if (customer.isPresent()) {
                logRepository.info("Customer found by idNumber, idNumber=" + idNumber
                        + ", customerId=" + customer.get().getCustomerId());
            } else {
                logRepository.info("Customer not found by idNumber, idNumber=" + idNumber);
            }
            return customer;
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            Error error = new Error("Get customer by idNumber error, idNumber=" + idNumber
                    + ", message=" + e.getMessage());
            logRepository.error(error);
            throw new RuntimeException(error);
        }
    }

    @Override
    public Customer addCustomer(String fullName, String idNumber, String phone, String email,
            CustomerType customerType) {
        logRepository.info("Adding new customer, fullName=" + fullName + ", idNumber=" + idNumber
                + ", phone=" + phone + ", email=" + email + ", customerType=" + customerType);

        if (fullName == null || fullName.trim().isEmpty()) {
            Error error = new Error("Add customer failed, fullName must not be null or empty");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (idNumber == null || idNumber.trim().isEmpty()) {
            Error error = new Error("Add customer failed, idNumber (ת.ז) must not be null or empty");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (phone == null || phone.trim().isEmpty()) {
            Error error = new Error("Add customer failed, phone must not be null or empty");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (email == null || email.trim().isEmpty()) {
            Error error = new Error("Add customer failed, email must not be null or empty");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (customerType == null) {
            Error error = new Error("Add customer failed, customerType must not be null");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (customerType == CustomerType.RETURNING) {
            Error error = new Error("Add customer failed, customerType RETURNING is not allowed");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Customer> existingByIdNumber = customerRepository.findByIdNumber(idNumber);
            if (existingByIdNumber.isPresent()) {
                Error error = new Error(
                        "Add customer failed, customer with idNumber (ת.ז) already exists: " + idNumber);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            Customer newCustomer = new Customer(fullName, idNumber, phone, email, customerType);

            customerRepository.save(newCustomer);

            logRepository.info("Add customer succeeded, customerId=" + newCustomer.getCustomerId()
                    + ", fullName=" + fullName + ", idNumber=" + idNumber + ", email=" + email
                    + ", customerType=" + customerType);

            return newCustomer;
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            Error error = new Error("Add customer error, fullName=" + fullName + ", idNumber=" + idNumber
                    + ", message=" + e.getMessage());
            logRepository.error(error);
            throw new RuntimeException(error);
        }
    }

}
