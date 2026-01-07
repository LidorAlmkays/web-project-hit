package shareddto.employeemanagement.request;

public class EmployeeGetRequest {
    private String email;

    public EmployeeGetRequest() {
    }

    public EmployeeGetRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
