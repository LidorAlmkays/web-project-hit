package server.api;

import shareddto.EventType;
import server.api.handlers.*;
import server.application.adaptors.*;

import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {
    private final AuthService authService;
    private final LoggerService logService;
    private final EmployeeService employeeService;
    private final BranchItemService branchItemService;
    private final BranchService branchService;
    private final CustomerService customerService;
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
        handlers.put(EventType.CREATE_EMPLOYEE, new CreateEmployeeHandler(employeeService));
        handlers.put(EventType.UPDATE_EMPLOYEE, new UpdateEmployeeHandler(employeeService));
        handlers.put(EventType.DELETE_EMPLOYEE, new DeleteEmployeeHandler(employeeService));
        handlers.put(EventType.GET_EMPLOYEE, new GetEmployeeHandler(employeeService));
        handlers.put(EventType.LIST_BRANCH_EMPLOYEES, new ListBranchEmployeesHandler());
        handlers.put(EventType.LOGOUT_EMPLOYEE, new LogoutEmployeeHandler(authService));

        // Chat Handlers
        server.api.handlers.SocketHandler chatHandler = server.chat.ChatManager.getInstance();
        handlers.put(EventType.CHAT_MESSAGE, chatHandler);
        handlers.put(EventType.CHAT_REQUEST, chatHandler); // If we use specific request packet later, we might need
                                                           // logic in handle() to distinguish
        handlers.put(EventType.MANAGER_JOIN, chatHandler);
        handlers.put(EventType.CHAT_CLOSE, chatHandler);
    }

    public SocketHandler createHandler(EventType eventType) {
        SocketHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler type: " + eventType);
        }
        return handler;
    }
}
