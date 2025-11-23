package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.customer.Customer;
import server.infustructre.adaptors.CustomerRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileCustomerRepository extends AbstractFileRepository<Customer>
        implements CustomerRepository {
    private final Map<UUID, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();
    private final Map<UUID, Customer> cache = Collections.synchronizedMap(new HashMap<>());
    // Index: email -> customerId for fast lookup
    private final Map<String, UUID> emailIndex = Collections.synchronizedMap(new HashMap<>());

    public FileCustomerRepository() {
        super(Config.getCustomersDir());
        loadCache();
    }

    private Object getLock(UUID customerId) {
        Object lock = locks.get(customerId);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = locks.get(customerId);
                if (lock == null) {
                    lock = new Object();
                    locks.put(customerId, lock);
                }
            }
        }
        return lock;
    }

    private String getFileName(UUID customerId) {
        return customerId.toString();
    }

    @Override
    protected String encode(Customer entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getCustomerId().toString()).append("\n");
        sb.append(entity.getFullName()).append("\n");
        sb.append(entity.getIdNumber()).append("\n");
        sb.append(entity.getPhone()).append("\n");
        sb.append(entity.getEmail()).append("\n");
        sb.append(entity.getCustomerType().name()).append("\n");
        sb.append(entity.getTotalPurchases()).append("\n");
        sb.append(entity.getTotalSpent()).append("\n");
        sb.append(entity.getRegistrationDate()).append("\n");
        return sb.toString();
    }

    @Override
    protected Customer decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 9) {
            throw new RuntimeException("Invalid customer format: insufficient data");
        }

        UUID customerId = UUID.fromString(lines[0].trim());
        String fullName = lines[1].trim();
        String idNumber = lines[2].trim();
        String phone = lines[3].trim();
        String email = lines[4].trim();
        server.domain.customer.CustomerType customerType = server.domain.customer.CustomerType.valueOf(lines[5].trim());
        int totalPurchases = Integer.parseInt(lines[6].trim());
        double totalSpent = Double.parseDouble(lines[7].trim());
        String registrationDate = lines[8].trim();

        return new Customer(customerId, fullName, idNumber, phone, email, customerType,
                totalPurchases, totalSpent, registrationDate);
    }

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        UUID customerId = customer.getCustomerId();
        String email = customer.getEmail();
        Object lock = getLock(customerId);
        synchronized (lock) {
            String fileName = getFileName(customerId);
            if (fileExists(fileName)) {
                throw new IllegalArgumentException("Customer with ID already exists: " + customerId);
            }
            if (emailIndex.containsKey(email.toLowerCase())) {
                throw new IllegalArgumentException("Customer with email already exists: " + email);
            }
            writeToFile(customer, fileName);
            cache.put(customerId, customer);
            emailIndex.put(email.toLowerCase(), customerId);
        }
    }

    @Override
    public void update(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        UUID customerId = customer.getCustomerId();
        String newEmail = customer.getEmail();
        Object lock = getLock(customerId);
        synchronized (lock) {
            Customer existingCustomer = cache.get(customerId);
            if (existingCustomer == null) {
                throw new IllegalArgumentException("Customer does not exist: " + customerId);
            }
            String oldEmail = existingCustomer.getEmail();

            // Check if email changed and new email already exists
            if (!oldEmail.equalsIgnoreCase(newEmail)) {
                if (emailIndex.containsKey(newEmail.toLowerCase())) {
                    throw new IllegalArgumentException("Customer with email already exists: " + newEmail);
                }
                emailIndex.remove(oldEmail.toLowerCase());
                emailIndex.put(newEmail.toLowerCase(), customerId);
            }

            String fileName = getFileName(customerId);
            writeToFile(customer, fileName);
            cache.put(customerId, customer.createCopy());
        }
    }

    @Override
    public void delete(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        Object lock = getLock(customerId);
        synchronized (lock) {
            Customer customer = cache.get(customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Customer does not exist: " + customerId);
            }
            String email = customer.getEmail();
            String fileName = getFileName(customerId);
            deleteFile(fileName);
            cache.remove(customerId);
            emailIndex.remove(email.toLowerCase());
        }
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        Object lock = getLock(customerId);
        synchronized (lock) {
            Customer customer = cache.get(customerId);
            if (customer == null) {
                return Optional.empty();
            }
            return Optional.of(customer.createCopy());
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        String emailLower = email.toLowerCase();
        UUID customerId;
        synchronized (emailIndex) {
            customerId = emailIndex.get(emailLower);
            if (customerId == null) {
                return Optional.empty();
            }
        }
        return findById(customerId);
    }

    private void loadCache() {
        synchronized (cache) {
            cache.clear();
            emailIndex.clear();
            java.util.List<Customer> allCustomers = readAllFromDirectory();
            for (Customer customer : allCustomers) {
                UUID customerId = customer.getCustomerId();
                String email = customer.getEmail();
                cache.put(customerId, customer);
                emailIndex.put(email.toLowerCase(), customerId);
            }
        }
    }
}
