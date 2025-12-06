package server.infustructre;

import server.infustructre.adaptors.*;
import server.infustructre.persistentTxtStorage.*;

public class InfrastructureFactory {

    public InfrastructureFactory() {
    }

    public BranchRepository createBranchRepository() {
        return new FileBranchRepository();
    }

    public CustomerRepository createCustomerRepository() {
        return new FileCustomerRepository();
    }

    public BranchInventoryItemRepository createBranchInventoryItemRepository() {
        return new FileBranchInventoryItemRepository();
    }

    public EmployeeRepository createEmployeeRepository() {
        return new FileEmployeeRepository();
    }

    public LogRepository createLogRepository() {
        return new FileLogRepository();
    }
}
