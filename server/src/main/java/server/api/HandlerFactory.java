package server.api;

import server.api.handlers.LoginEmployeeHandler;
import server.api.handlers.LogoutEmployeeHandler;
import server.api.handlers.ReportHandler;
import server.api.handlers.SocketHandler;
import server.application.adaptors.*;
import shareddto.EventType;

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
    private final Map<EventType, SocketHandler> handlers;

    public HandlerFactory(AuthService authService, LoggerService logService, EmployeeService employeeService,
                          BranchItemService branchItemService, BranchService branchService, CustomerService customerService, ReportService reportService) {
        this.branchItemService = branchItemService;
        this.branchService = branchService;
        this.authService = authService;
        this.logService = logService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.reportService = reportService;
        this.handlers = new HashMap<>();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(EventType.LOGIN_EMPLOYEE, new LoginEmployeeHandler(authService));
        handlers.put(EventType.LOGOUT_EMPLOYEE, new LogoutEmployeeHandler(authService));

        // --- System Logs Handlers ---
        handlers.put(EventType.GET_SYSTEM_LOGS_JSON, new SystemLogHandler(logService, SystemLogHandler.LogRequestType.JSON));
        handlers.put(EventType.GET_SYSTEM_LOGS_DOCUMENT, new SystemLogHandler(logService, SystemLogHandler.LogRequestType.DOCUMENT));

        // --- Business Reports Handlers ---
        handlers.put(EventType.GET_BRANCH_INVENTORY_REPORT, new ReportHandler(reportService, ReportHandler.ReportType.BRANCH_INVENTORY));
        handlers.put(EventType.GET_SALES_STATS_BRANCH, new ReportHandler(reportService, ReportHandler.ReportType.SALES_STATS_BRANCH));
        handlers.put(EventType.GET_SALES_STATS_PRODUCT, new ReportHandler(reportService, ReportHandler.ReportType.SALES_STATS_PRODUCT));
        
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
