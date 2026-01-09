package server.api.handlers;

import shareddto.employeemanagement.response.EmployeeDto;
import server.domain.employee.Employee;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeMapper {
    private EmployeeMapper() {
    }

    public static EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }
        String branchId = employee.getBranchId() != null ? employee.getBranchId().toString() : null;
        return new EmployeeDto(
                employee.getEmployeeNumber().toString(),
                branchId,
                employee.getFullName(),
                employee.getEmployeeId(),
                employee.getPhoneNumber(),
                employee.getBankAccountNumber(),
                employee.getRole().name(),
                employee.getEmail());
    }

    public static List<EmployeeDto> toDtoList(List<Employee> employees) {
        List<EmployeeDto> responses = new ArrayList<>();
        if (employees == null) {
            return responses;
        }
        for (Employee employee : employees) {
            responses.add(toDto(employee));
        }
        return responses;
    }
}
