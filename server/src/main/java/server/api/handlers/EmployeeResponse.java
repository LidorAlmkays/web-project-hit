package server.api.handlers;

import server.domain.employee.Employee;

import java.util.ArrayList;
import java.util.List;

public class EmployeeResponse {
    private String employeeNumber;
    private String branchId;
    private String fullName;
    private String employeeId;
    private String phoneNumber;
    private String bankAccountNumber;
    private String role;
    private String email;

    public EmployeeResponse() {
    }

    public EmployeeResponse(String employeeNumber, String branchId, String fullName, String employeeId,
            String phoneNumber, String bankAccountNumber, String role, String email) {
        this.employeeNumber = employeeNumber;
        this.branchId = branchId;
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.phoneNumber = phoneNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.role = role;
        this.email = email;
    }

    public static EmployeeResponse from(Employee employee) {
        if (employee == null) {
            return null;
        }
        String branchId = employee.getBranchId() != null ? employee.getBranchId().toString() : null;
        return new EmployeeResponse(
                employee.getEmployeeNumber().toString(),
                branchId,
                employee.getFullName(),
                employee.getEmployeeId(),
                employee.getPhoneNumber(),
                employee.getBankAccountNumber(),
                employee.getRole().name(),
                employee.getEmail());
    }

    public static List<EmployeeResponse> fromList(List<Employee> employees) {
        List<EmployeeResponse> responses = new ArrayList<>();
        if (employees == null) {
            return responses;
        }
        for (Employee employee : employees) {
            responses.add(from(employee));
        }
        return responses;
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
}
