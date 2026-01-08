package shareddto.employeemanagement.request;

public class EmployeeDeleteRequest {
    private String employeeNumber;

    public EmployeeDeleteRequest() {
    }

    public EmployeeDeleteRequest(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}
