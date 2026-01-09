package server.api;

import server.api.handlers.BuyItemHandler;
import server.api.handlers.GetBranchInfoHandler;
import server.api.handlers.GetInventoryItemsHandler;
import server.api.handlers.LoginEmployeeHandler;
import server.api.handlers.LogoutEmployeeHandler;
import server.api.handlers.SocketHandler;
import server.api.handlers.UpdateInventoryItemHandler;
import server.application.adaptors.*;
import shareddto.EventType;

import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {
    private final AuthService authService;
    private final LoggerService logService;
    private final EmployeeService employeeService;
    private final BranchService branchService;
    private final CustomerService customerService;
    private final BranchItemService branchItemService;
    private final Map<EventType, SocketHandler> handlers;

    public HandlerFactory(AuthService authService, LoggerService logService, EmployeeService employeeService,
                          BranchItemService branchItemService, BranchService branchService, CustomerService customerService) {
        this.branchItemService = branchItemService;
        this.branchService = branchService;
        this.authService = authService;
        this.logService = logService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.handlers = new HashMap<>();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(EventType.LOGIN_EMPLOYEE, new LoginEmployeeHandler(authService));
        handlers.put(EventType.GET_BRANCH_INFO, new GetBranchInfoHandler(branchService));
        handlers.put(EventType.GET_INVERTORY_ITEMS, new GetInventoryItemsHandler(branchItemService));
        handlers.put(EventType.BUY_INVENTORY_ITEM, new BuyItemHandler(branchItemService));
        handlers.put(EventType.UPDATE_INVENTORY_ITEM, new UpdateInventoryItemHandler(branchItemService));
        handlers.put(EventType.LOGOUT_EMPLOYEE, new LogoutEmployeeHandler(authService));
    }

    public SocketHandler createHandler(EventType eventType) {
        SocketHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler type: " + eventType);
        }
        return handler;
    }
}
