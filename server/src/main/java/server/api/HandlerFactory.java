package server.api;

import shareddto.EventType;
import server.api.handlers.AddInventoryItemHandler;
import server.api.handlers.GetBranchInfoHandler;
import server.api.handlers.GetInventoryItemsHandler;
import server.api.handlers.LoginEmployeeHandler;
import server.api.handlers.SocketHandler;
import server.api.handlers.UpdateInventoryItemHandler;
import server.application.adaptors.AuthService;
import server.application.adaptors.BranchItemService;
import server.application.adaptors.BranchService;
import server.application.adaptors.EmployeeService;
import server.application.adaptors.LoggerService;

import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {
    private final AuthService authService;
    private final LoggerService logService;
    private final EmployeeService employeeService;
    private final BranchService branchService;
    private final BranchItemService branchItemService;
    private final Map<EventType, SocketHandler> handlers;

    public HandlerFactory(AuthService authService, LoggerService logService, EmployeeService employeeService, BranchService branchService, BranchItemService branchItemService) {
        this.authService = authService;
        this.logService = logService;
        this.employeeService = employeeService;
        this.branchService = branchService;
        this.branchItemService = branchItemService;
        this.handlers = new HashMap<>();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(EventType.LOGIN_EMPLOYEE, new LoginEmployeeHandler(authService));
        handlers.put(EventType.GET_BRANCH_INFO, new GetBranchInfoHandler(branchService));
        handlers.put(EventType.GET_INVERTORY_ITEMS, new GetInventoryItemsHandler(branchItemService));
        handlers.put(EventType.ADD_INVENTORY_ITEM, new AddInventoryItemHandler(branchItemService));
        handlers.put(EventType.UPDATE_INVENTORY_ITEM, new UpdateInventoryItemHandler(branchItemService));
    }

    public SocketHandler createHandler(EventType eventType) {
        SocketHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler type: " + eventType);
        }
        return handler;
    }
}
