package shareddto.employeemanagement.request;

public class EmployeeDeleteRequest {
    private String email;

    public EmployeeDeleteRequest() {
    }

    public EmployeeDeleteRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
