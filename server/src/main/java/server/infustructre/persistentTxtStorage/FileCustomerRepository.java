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
    // idNumber -> customerId
    private final Map<String, UUID> idNumberIndex = Collections.synchronizedMap(new HashMap<>());

    public FileCustomerRepository() {
        super(Config.CUSTOMERS_DIR);
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
        return sb.toString();
    }

    @Override
    protected Customer decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 8) {
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

        return new Customer(customerId, fullName, idNumber, phone, email, customerType,
                totalPurchases, totalSpent);
    }

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("cant save, consumer is null");
        }
        UUID customerId = customer.getCustomerId();
        Object lock = getLock(customerId);
        synchronized (lock) {
            String fileName = getFileName(customerId);
            if (fileExists(fileName)) {
                throw new IllegalArgumentException("cant save customer, id already exists: " + customerId);
            }
            String idNumber = customer.getIdNumber();
            if (idNumberIndex.containsKey(idNumber)) {
                throw new IllegalArgumentException("cant save consumer, idNumber already exists: " + idNumber);
            }
            writeToFile(customer, fileName);
            cache.put(customerId, customer);
            idNumberIndex.put(idNumber, customerId);
        }
    }

    @Override
    public void update(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("cant update, consumer is null");
        }
        UUID customerId = customer.getCustomerId();
        Object lock = getLock(customerId);
        synchronized (lock) {
            Customer existingCustomer = cache.get(customerId);
            if (existingCustomer == null) {
                throw new IllegalArgumentException("cant update, consumer not found, id: " + customerId);
            }

            String fileName = getFileName(customerId);
            writeToFile(customer, fileName);
            cache.put(customerId, customer.createCopy());
        }
    }

    @Override
    public void delete(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("cant delete, customer id cant be null");
        }
        Object lock = getLock(customerId);
        synchronized (lock) {
            Customer customer = cache.get(customerId);
            if (customer == null) {
                throw new IllegalArgumentException("cant delete, customer not found, id: " + customerId);
            }
            String idNumber = customer.getIdNumber();
            String fileName = getFileName(customerId);
            deleteFile(fileName);
            cache.remove(customerId);
            idNumberIndex.remove(idNumber);
        }
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("cant find, customer id cant be null");
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
    public Optional<Customer> findByIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("cant find, customer idNumber cant be null or empty");
        }
        UUID customerId;
        synchronized (idNumberIndex) {
            customerId = idNumberIndex.get(idNumber);
            if (customerId == null) {
                return Optional.empty();
            }
        }
        return findById(customerId);
    }

    private void loadCache() {
        synchronized (cache) {
            cache.clear();
            idNumberIndex.clear();
            java.util.List<Customer> allCustomers = readAllFromDirectory();
            for (Customer customer : allCustomers) {
                UUID customerId = customer.getCustomerId();
                String idNumber = customer.getIdNumber();
                cache.put(customerId, customer);
                idNumberIndex.put(idNumber, customerId);
            }
        }
    }
}
