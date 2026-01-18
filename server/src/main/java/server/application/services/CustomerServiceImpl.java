package server.application.services;

import server.application.adaptors.CustomerService;
import server.domain.LogEntry;
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
        logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET CUSTOMER] by idNumber (ת.ז): " + idNumber);

        if (idNumber == null || idNumber.trim().isEmpty()) {
            String errorMessage = "[GET CUSTOMER] failed, idNumber is null or empty";
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            Optional<Customer> customer = customerRepository.findByIdNumber(idNumber);
            if (customer.isPresent()) {
                logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET CUSTOMER] found, idNumber=" + idNumber
                        + ", customerId=" + customer.get().getCustomerId());
            } else {
                logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET CUSTOMER] not found, idNumber=" + idNumber);
            }
            return customer;
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            String errorMessage = "[GET CUSTOMER] failed, idNumber=" + idNumber
                    + ", message=" + e.getMessage();
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    @Override
    public Customer addCustomer(String fullName, String idNumber, String phone, String email) {
        CustomerType customerType = CustomerType.NEW;
        logRepository.info(LogEntry.LogType.MANAGEMENT, "[ADD CUSTOMER] adding new customer, fullName=" + fullName + ", idNumber=" + idNumber
                + ", phone=" + phone + ", email=" + email + ", customerType=" + customerType);

        if (fullName == null || fullName.trim().isEmpty()) {
            String errorMessage = "[ADD CUSTOMER] failed, fullName must not be null or empty";
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if (idNumber == null || idNumber.trim().isEmpty()) {
            String errorMessage = "[ADD CUSTOMER] failed, idNumber (ת.ז) must not be null or empty";
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if (phone == null || phone.trim().isEmpty()) {
            String errorMessage = "[ADD CUSTOMER] failed, phone must not be null or empty";
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if (email == null || email.trim().isEmpty()) {
            String errorMessage = "[ADD CUSTOMER] failed, email must not be null or empty";
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            Optional<Customer> existingByIdNumber = customerRepository.findByIdNumber(idNumber);
            if (existingByIdNumber.isPresent()) {
                String errorMessage = "[ADD CUSTOMER] failed, customer with idNumber (ת.ז) already exists: " + idNumber;
                logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            Customer newCustomer = new Customer(fullName, idNumber, phone, email, customerType);

            customerRepository.save(newCustomer);

            logRepository.info(LogEntry.LogType.MANAGEMENT, "[ADD CUSTOMER] Successful, customerId=" + newCustomer.getCustomerId()
                    + ", fullName=" + fullName + ", idNumber=" + idNumber + ", email=" + email
                    + ", customerType=" + customerType);

            return newCustomer;
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            String errorMessage = "[ADD CUSTOMER] failed, fullName=" + fullName + ", idNumber=" + idNumber
                    + ", message=" + e.getMessage();
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

}
