package shareddto.customermanagement.request;


public class CustomerCreateRequest {
    private String fullName;
    private String idNumber;
    private String phone;
    private String email;

    public CustomerCreateRequest() {
    }

    public CustomerCreateRequest(String fullName, String idNumber, String phone, String email) {
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
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
}
