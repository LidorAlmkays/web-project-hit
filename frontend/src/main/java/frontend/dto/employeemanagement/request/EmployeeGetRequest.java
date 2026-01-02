package frontend.dto.employeemanagement.request;

public class EmployeeGetRequest {
    private String employeeNumber;

    public EmployeeGetRequest() {
    }

    public EmployeeGetRequest(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}
