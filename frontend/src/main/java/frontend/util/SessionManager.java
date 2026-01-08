package frontend.util;

import shareddto.employeemanagement.response.EmployeeDto;

public class SessionManager {
    private static SessionManager instance;
    private EmployeeDto currentEmployee;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentEmployee(EmployeeDto currentEmployee) {
        this.currentEmployee = currentEmployee;
    }

    public EmployeeDto getCurrentEmployee() {
        return currentEmployee;
    }

    public void logout() {
        this.currentEmployee = null;
    }
}
