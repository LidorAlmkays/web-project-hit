package shareddto.customermanagement.request;

/**
 * Request payload for creating a customer.
 */
public class CustomerCreateRequest {
    private String fullName;
    private String idNumber;
    private String phone;
    private String email;
    private String customerType;

    public CustomerCreateRequest() {
    }

    public CustomerCreateRequest(String fullName, String idNumber, String phone, String email, String customerType) {
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
        this.customerType = customerType;
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
}
