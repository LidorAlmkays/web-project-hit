package server.domain.customer;

import java.util.UUID;

public class Customer {
    private final UUID customerId;// index
    private String fullName;
    private String idNumber;
    private String phone;
    private String email;
    private CustomerType customerType;
    private int totalPurchases;
    private double totalSpent;
    private String registrationDate;

    public Customer(String fullName, String idNumber, String phone, String email, CustomerType customerType) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName must not be null or empty");
        }
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("idNumber must not be null or empty");
        }
        if (phone == null) {
            throw new IllegalArgumentException("phone must not be null");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        if (customerType == null) {
            throw new IllegalArgumentException("customerType must not be null");
        }
        this.customerId = UUID.randomUUID();
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
        this.customerType = customerType;
        this.totalPurchases = 0;
        this.totalSpent = 0.0;
        this.registrationDate = String.valueOf(System.currentTimeMillis());
    }

    public Customer(UUID customerId, String fullName, String idNumber, String phone, String email,
            CustomerType customerType,
            int totalPurchases, double totalSpent, String registrationDate) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName must not be null or empty");
        }
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("idNumber must not be null or empty");
        }
        if (phone == null) {
            throw new IllegalArgumentException("phone must not be null");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        if (customerType == null) {
            throw new IllegalArgumentException("customerType must not be null");
        }
        if (totalPurchases < 0) {
            throw new IllegalArgumentException("totalPurchases must be non-negative");
        }
        if (totalSpent < 0) {
            throw new IllegalArgumentException("totalSpent must be non-negative");
        }
        if (registrationDate == null) {
            throw new IllegalArgumentException("registrationDate must not be null");
        }
        this.customerId = customerId;
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
        this.customerType = customerType;
        this.totalPurchases = totalPurchases;
        this.totalSpent = totalSpent;
        this.registrationDate = registrationDate;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public int getTotalPurchases() {
        return totalPurchases;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName must not be null or empty");
        }
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("phone must not be null");
        }
        this.phone = phone;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        this.email = email;
    }

    public void setCustomerType(CustomerType customerType) {
        if (customerType == null) {
            throw new IllegalArgumentException("customerType must not be null");
        }
        this.customerType = customerType;
    }

    public void addPurchase(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        this.totalPurchases++;
        this.totalSpent += amount;
    }

    public double calculateFinalPrice(double originalPrice) {
        CustomerPaymentStrategy strategy = CustomerPaymentStrategyFactory.createStrategy(this.customerType);
        return strategy.calculateFinalPrice(originalPrice);
    }

    public double calculateDiscount(double originalPrice) {
        CustomerPaymentStrategy strategy = CustomerPaymentStrategyFactory.createStrategy(this.customerType);
        return strategy.calculateDiscount(originalPrice);
    }

    public Customer createCopy() {
        return new Customer(
                this.customerId,
                this.fullName,
                this.idNumber,
                this.phone,
                this.email,
                this.customerType,
                this.totalPurchases,
                this.totalSpent,
                this.registrationDate);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", fullName='" + fullName + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", customerType=" + customerType +
                ", totalPurchases=" + totalPurchases +
                ", totalSpent=" + totalSpent +
                '}';
    }
}
