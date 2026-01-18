package server.application.services;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import server.application.adaptors.CustomerService;
import server.domain.customer.Customer;
import server.domain.customer.CustomerType;
import server.domain.LogEntry;
import server.infustructre.adaptors.CustomerRepository;
import server.infustructre.adaptors.LogRepository;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class CustomerServiceImplTest {

    private CustomerService customerService;
    private MockCustomerRepository mockCustomerRepository;
    private MockLogRepository mockLogRepository;

    @Before
    public void setUp() {
        System.out.println("Setting up test...");
        mockCustomerRepository = new MockCustomerRepository();
        mockLogRepository = new MockLogRepository();
        customerService = new CustomerServiceImpl(mockCustomerRepository, mockLogRepository);
    }

    @After
    public void tearDown() {
        System.out.println("Cleaning up after test...");
        mockCustomerRepository.reset();
        mockLogRepository.reset();
    }

    @Test
    public void testGetCustomerByIdNumber_Success() {
        System.out.println("Testing getCustomerByIdNumber - success case");
        
        // Arrange
        String idNumber = "123456789";
        Customer expectedCustomer = new Customer("John Doe", idNumber, "0501234567", "john@example.com", CustomerType.NEW);
        mockCustomerRepository.addCustomer(expectedCustomer);

        // Act
        Optional<Customer> result = customerService.getCustomerByIdNumber(idNumber);

        // Assert
        assertTrue("Customer should be found", result.isPresent());
        assertEquals("Customer ID number should match", idNumber, result.get().getIdNumber());
        assertEquals("Customer name should match", "John Doe", result.get().getFullName());
        assertTrue("Log repository should have been called", mockLogRepository.hasInfoLogs());
    }

    @Test
    public void testGetCustomerByIdNumber_NotFound() {
        System.out.println("Testing getCustomerByIdNumber - not found case");
        
        // Arrange
        String idNumber = "999999999";

        // Act
        Optional<Customer> result = customerService.getCustomerByIdNumber(idNumber);

        // Assert
        assertFalse("Customer should not be found", result.isPresent());
        assertTrue("Log repository should have been called", mockLogRepository.hasInfoLogs());
    }

    @Test
    public void testGetCustomerByIdNumber_NullIdNumber() {
        System.out.println("Testing getCustomerByIdNumber - null idNumber");
        
        // Arrange
        String idNumber = null;

        // Act & Assert
        try {
            customerService.getCustomerByIdNumber(idNumber);
            fail("Should have thrown IllegalArgumentException for null idNumber");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
            assertTrue("Error message should contain relevant info", 
                       e.getMessage().contains("idNumber") || e.getMessage().contains("null") || e.getMessage().contains("empty"));
        }
    }

    @Test
    public void testGetCustomerByIdNumber_EmptyIdNumber() {
        System.out.println("Testing getCustomerByIdNumber - empty idNumber");
        
        // Arrange
        String idNumber = "";

        // Act & Assert
        try {
            customerService.getCustomerByIdNumber(idNumber);
            fail("Should have thrown IllegalArgumentException for empty idNumber");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
        }
    }

    @Test
    public void testGetCustomerByIdNumber_WhitespaceIdNumber() {
        System.out.println("Testing getCustomerByIdNumber - whitespace idNumber");
        
        // Arrange
        String idNumber = "   ";

        // Act & Assert
        try {
            customerService.getCustomerByIdNumber(idNumber);
            fail("Should have thrown IllegalArgumentException for whitespace idNumber");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
        }
    }

    @Test
    public void testAddCustomer_Success() {
        System.out.println("Testing addCustomer - success case");
        
        // Arrange
        String fullName = "Jane Smith";
        String idNumber = "987654321";
        String phone = "0529876543";
        String email = "jane@example.com";

        // Act
        Customer result = customerService.addCustomer(fullName, idNumber, phone, email);

        // Assert
        assertNotNull("Customer should not be null", result);
        assertEquals("Full name should match", fullName, result.getFullName());
        assertEquals("ID number should match", idNumber, result.getIdNumber());
        assertEquals("Phone should match", phone, result.getPhone());
        assertEquals("Email should match", email, result.getEmail());
        assertEquals("Customer type should be NEW", CustomerType.NEW, result.getCustomerType());
        assertNotNull("Customer ID should be generated", result.getCustomerId());
        assertTrue("Customer should be saved in repository", mockCustomerRepository.wasSaveCalled());
        assertTrue("Success should be logged", mockLogRepository.hasInfoLogs());
    }

    @Test
    public void testAddCustomer_NullFullName() {
        System.out.println("Testing addCustomer - null fullName");
        
        // Arrange
        String fullName = null;
        String idNumber = "123456789";
        String phone = "0501234567";
        String email = "test@example.com";

        // Act & Assert
        try {
            customerService.addCustomer(fullName, idNumber, phone, email);
            fail("Should have thrown IllegalArgumentException for null fullName");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
            assertFalse("Customer should not be saved", mockCustomerRepository.wasSaveCalled());
        }
    }

    @Test
    public void testAddCustomer_EmptyFullName() {
        System.out.println("Testing addCustomer - empty fullName");
        
        // Arrange
        String fullName = "";
        String idNumber = "123456789";
        String phone = "0501234567";
        String email = "test@example.com";

        // Act & Assert
        try {
            customerService.addCustomer(fullName, idNumber, phone, email);
            fail("Should have thrown IllegalArgumentException for empty fullName");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
        }
    }

    @Test
    public void testAddCustomer_NullIdNumber() {
        System.out.println("Testing addCustomer - null idNumber");
        
        // Arrange
        String fullName = "Test User";
        String idNumber = null;
        String phone = "0501234567";
        String email = "test@example.com";

        // Act & Assert
        try {
            customerService.addCustomer(fullName, idNumber, phone, email);
            fail("Should have thrown IllegalArgumentException for null idNumber");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
        }
    }

    @Test
    public void testAddCustomer_NullPhone() {
        System.out.println("Testing addCustomer - null phone");
        
        // Arrange
        String fullName = "Test User";
        String idNumber = "123456789";
        String phone = null;
        String email = "test@example.com";

        // Act & Assert
        try {
            customerService.addCustomer(fullName, idNumber, phone, email);
            fail("Should have thrown IllegalArgumentException for null phone");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
        }
    }

    @Test
    public void testAddCustomer_NullEmail() {
        System.out.println("Testing addCustomer - null email");
        
        // Arrange
        String fullName = "Test User";
        String idNumber = "123456789";
        String phone = "0501234567";
        String email = null;

        // Act & Assert
        try {
            customerService.addCustomer(fullName, idNumber, phone, email);
            fail("Should have thrown IllegalArgumentException for null email");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
        }
    }

    @Test
    public void testAddCustomer_DuplicateIdNumber() {
        System.out.println("Testing addCustomer - duplicate idNumber");
        
        // Arrange
        String idNumber = "123456789";
        Customer existingCustomer = new Customer("Existing User", idNumber, "0501111111", "existing@example.com", CustomerType.RETURNING);
        mockCustomerRepository.addCustomer(existingCustomer);

        String fullName = "New User";
        String phone = "0501234567";
        String email = "new@example.com";

        // Act & Assert
        try {
            customerService.addCustomer(fullName, idNumber, phone, email);
            fail("Should have thrown IllegalArgumentException for duplicate idNumber");
        } catch (IllegalArgumentException e) {
            assertTrue("Error should be logged", mockLogRepository.hasErrorLogs());
            assertFalse("Customer should not be saved", mockCustomerRepository.wasSaveCalled());
        }
    }

    private static class MockCustomerRepository implements CustomerRepository {
        private List<Customer> customers = new ArrayList<>();
        private boolean saveCalled = false;

        public void addCustomer(Customer customer) {
            customers.add(customer);
        }

        public boolean wasSaveCalled() {
            return saveCalled;
        }

        public void reset() {
            customers.clear();
            saveCalled = false;
        }

        @Override
        public void save(Customer customer) {
            saveCalled = true;
            customers.add(customer);
        }

        @Override
        public void update(Customer customer) {}

        @Override
        public void delete(java.util.UUID customerId) {}

        @Override
        public Optional<Customer> findById(java.util.UUID customerId) {
            return customers.stream()
                    .filter(c -> c.getCustomerId().equals(customerId))
                    .findFirst();
        }

        @Override
        public Optional<Customer> findByIdNumber(String idNumber) {
            return customers.stream()
                    .filter(c -> c.getIdNumber().equals(idNumber))
                    .findFirst();
        }
    }

    private static class MockLogRepository implements LogRepository {
        private List<String> infoLogs = new ArrayList<>();
        private List<String> errorLogs = new ArrayList<>();
        private List<LogEntry> savedEntries = new ArrayList<>();

        public boolean hasInfoLogs() {
            return !infoLogs.isEmpty();
        }

        public boolean hasErrorLogs() {
            return !errorLogs.isEmpty();
        }

        public void reset() {
            infoLogs.clear();
            errorLogs.clear();
            savedEntries.clear();
        }

        @Override
        public void save(LogEntry entry) {
            savedEntries.add(entry);
        }

        @Override
        public List<LogEntry> findAll() {
            return new ArrayList<>(savedEntries);
        }

        @Override
        public void info(LogEntry.LogType type, String message) {
            infoLogs.add(message);
            System.out.println("[INFO] " + type + " | " + message);
        }

        @Override
        public void info(LogEntry.LogType type, String userId, String message) {
            infoLogs.add(message);
            System.out.println("[INFO] " + type + " | User: " + userId + " | " + message);
        }

        @Override
        public void error(LogEntry.LogType type, String message) {
            errorLogs.add(message);
            System.out.println("[ERROR] " + type + " | " + message);
        }

        @Override
        public void error(LogEntry.LogType type, String userId, String message) {
            errorLogs.add(message);
            System.out.println("[ERROR] " + type + " | User: " + userId + " | " + message);
        }
    }
}
