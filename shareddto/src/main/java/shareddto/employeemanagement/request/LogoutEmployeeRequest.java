package shareddto.employeemanagement.request;

public class LogoutEmployeeRequest {
    private String employeeNumber;

    public LogoutEmployeeRequest() {
    }

    public LogoutEmployeeRequest(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}
