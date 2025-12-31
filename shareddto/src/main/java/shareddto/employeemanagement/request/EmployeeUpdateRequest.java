package shareddto.employeemanagement.request;

/**
 * Request payload for updating an employee.
 */
public class EmployeeUpdateRequest {
    private String employeeNumber;
    private String branchId;
    private String fullName;
    private String employeeId;
    private String phoneNumber;
    private String bankAccountNumber;
    private String role;
    private String email;
    private String password;

    public EmployeeUpdateRequest() {
    }

    public EmployeeUpdateRequest(String employeeNumber, String branchId, String fullName, String employeeId,
            String phoneNumber, String bankAccountNumber, String role, String email, String password) {
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

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
