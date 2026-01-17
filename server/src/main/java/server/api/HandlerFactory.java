package server.api;

import shareddto.EventType;
import server.api.handlers.*;
import server.api.handlers.chat.*;
import server.application.adaptors.*;
import server.infustructre.adaptors.EmployeeRepository;

import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {
    private final AuthService authService;
    private final LoggerService logService;
    private final EmployeeService employeeService;
    private final BranchItemService branchItemService;
    private final BranchService branchService;
    private final CustomerService customerService;
    private final ReportService reportService;
    private final EmployeeRepository employeeRepository;
    private final Map<EventType, SocketHandler> handlers;

    public HandlerFactory(AuthService authService, LoggerService logService, EmployeeService employeeService,
            BranchItemService branchItemService, BranchService branchService, CustomerService customerService,
            ReportService reportService, EmployeeRepository employeeRepository) {
        this.branchItemService = branchItemService;
        this.branchService = branchService;
        this.authService = authService;
        this.logService = logService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.reportService = reportService;
        this.employeeRepository = employeeRepository;
        this.handlers = new HashMap<>();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(EventType.LOGIN_EMPLOYEE, new LoginEmployeeHandler(authService));
        handlers.put(EventType.GET_BRANCH_INFO, new GetBranchInfoHandler(branchService));
        handlers.put(EventType.GET_INVERTORY_ITEMS, new GetInventoryItemsHandler(branchItemService));
        handlers.put(EventType.BUY_INVENTORY_ITEM, new BuyItemHandler(branchItemService));
        handlers.put(EventType.UPDATE_INVENTORY_ITEM, new UpdateInventoryItemHandler(branchItemService));
        handlers.put(EventType.CREATE_EMPLOYEE, new CreateEmployeeHandler(employeeService));
        handlers.put(EventType.UPDATE_EMPLOYEE, new UpdateEmployeeHandler(employeeService));
        handlers.put(EventType.DELETE_EMPLOYEE, new DeleteEmployeeHandler(employeeService));
        handlers.put(EventType.GET_EMPLOYEE, new GetEmployeeHandler(employeeService));
        handlers.put(EventType.LIST_BRANCH_EMPLOYEES, new ListBranchEmployeesHandler());
        handlers.put(EventType.LOGOUT_EMPLOYEE, new LogoutEmployeeHandler(authService));

        // --- System Logs Handlers ---
        handlers.put(EventType.GET_SYSTEM_LOGS_JSON, new SystemLogHandler(logService));
        
        // --- Business Reports Handlers ---
        handlers.put(EventType.GET_BRANCH_INVENTORY_REPORT, new ReportHandler(reportService, ReportHandler.ReportType.BRANCH_INVENTORY));
        handlers.put(EventType.GET_SALES_STATS_BRANCH, new ReportHandler(reportService, ReportHandler.ReportType.SALES_STATS_BRANCH));
        handlers.put(EventType.GET_SALES_STATS_PRODUCT, new ReportHandler(reportService, ReportHandler.ReportType.SALES_STATS_PRODUCT));

        handlers.put(EventType.ADD_INVENTORY_ITEM, new AddItemHandler(branchItemService));
        handlers.put(EventType.GET_CUSTOMER, new GetCustomerHandler(customerService));
        handlers.put(EventType.CHAT_REQUEST, new StartBranchChatHandler());
        handlers.put(EventType.CHAT_MESSAGE, new SendMessageHandler());
        handlers.put(EventType.CHAT_CLOSE, new CloseChatHandler());
        handlers.put(EventType.CHAT_ACCEPT, new AcceptChatHandler());
        handlers.put(EventType.CHAT_DECLINE, new DeclineChatHandler());
        handlers.put(EventType.GET_PENDING_REQUESTS, new GetPendingRequestsHandler());
        handlers.put(EventType.GET_ACTIVE_CHATS, new GetActiveChatsHandler(employeeRepository));
        handlers.put(EventType.MANAGER_JOIN, new JoinChatHandler());
    }

    public SocketHandler createHandler(EventType eventType) {
        SocketHandler handler = handlers.get(eventType);
        if (handler == null) {
            System.err.println("Warning: No handler found for event type: " + eventType);
            throw new IllegalArgumentException("Invalid handler type: " + eventType);
            }
        return handler;
    }
}
