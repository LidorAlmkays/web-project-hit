package shareddto.customermanagement.response;

/**
 * Customer data transfer object for CLI output.
 */
public class CustomerDto {
    private String customerId;
    private String fullName;
    private String idNumber;
    private String phone;
    private String email;
    private String customerType;
    private int totalPurchases;
    private double totalSpent;

    public CustomerDto() {
    }

    public CustomerDto(String customerId, String fullName, String idNumber, String phone, String email,
            String customerType, int totalPurchases, double totalSpent) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
        this.customerType = customerType;
        this.totalPurchases = totalPurchases;
        this.totalSpent = totalSpent;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public int getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(int totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }
}
