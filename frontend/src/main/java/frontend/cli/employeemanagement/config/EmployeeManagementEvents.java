package frontend.cli.employeemanagement.config;

import shareddto.EventType;

/**
 * Socket event types for employee management operations.
 */
public final class EmployeeManagementEvents {
    public static final EventType CREATE_EMPLOYEE = EventType.CREATE_EMPLOYEE;
    public static final EventType UPDATE_EMPLOYEE = EventType.UPDATE_EMPLOYEE;
    public static final EventType DELETE_EMPLOYEE = EventType.DELETE_EMPLOYEE;
    public static final EventType GET_EMPLOYEE = EventType.GET_EMPLOYEE;
    public static final EventType LIST_BRANCH_EMPLOYEES = EventType.LIST_BRANCH_EMPLOYEES;

    private EmployeeManagementEvents() {
    }
}
