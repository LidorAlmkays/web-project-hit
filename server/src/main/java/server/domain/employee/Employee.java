package server.domain.employee;

import java.util.UUID;

public class Employee {
    private final UUID employeeNumber; // index
    private UUID branchId; // index
    private String fullName;
    private String employeeId; // ת.ז
    private String phoneNumber;
    private String bankAccountNumber;
    private EmployeeRole role;
    private String email; // index + username
    private String password; // normal string

    public Employee(UUID branchId, String fullName, String employeeId, String phoneNumber,
            String bankAccountNumber, EmployeeRole role,
            String email, String password) {

        if (branchId == null && role != EmployeeRole.ADMIN) {
            throw new IllegalArgumentException("branchId must not be null for non-admin employees");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName must not be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("password must not be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }

        this.employeeNumber = UUID.randomUUID();
        this.branchId = branchId;
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.phoneNumber = phoneNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public Employee(UUID employeeNumber, UUID branchId, String fullName, String employeeId, String phoneNumber,
            String bankAccountNumber, EmployeeRole role,
            String email, String password) {

        if (employeeNumber == null) {
            throw new IllegalArgumentException("employeeNumber must not be null");
        }
        if (branchId == null && role != EmployeeRole.ADMIN) {
            throw new IllegalArgumentException("branchId must not be null for non-admin employees");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName must not be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("password must not be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }

        this.employeeNumber = employeeNumber;
        this.branchId = branchId;
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.phoneNumber = phoneNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    // getters

    public UUID getEmployeeNumber() {
        return employeeNumber;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // setters

    public void setBranchId(UUID branchId) {
        if (branchId == null && this.role != EmployeeRole.ADMIN) {
            throw new IllegalArgumentException("branchId must not be null for non-admin employees");
        }
        this.branchId = branchId;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName must not be null or empty");
        }
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public void setRole(EmployeeRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.role = role;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be null or empty");
        }
        this.email = email;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("password must not be null or empty");
        }
        this.password = password;
    }

    public Employee createCopy() {
        return new Employee(
                this.employeeNumber,
                this.branchId,
                this.fullName,
                this.employeeId,
                this.phoneNumber,
                this.bankAccountNumber,
                this.role,
                this.email,
                this.password);
    }
}
