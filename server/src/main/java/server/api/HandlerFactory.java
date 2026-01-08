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
        handlers.put(EventType.GET_DAILY_REPORT_JSON, new ReportHandler(reportService, ReportHandler.ReportType.DAILY_JSON));
        handlers.put(EventType.GET_DAILY_REPORT_WORD, new ReportHandler(reportService, ReportHandler.ReportType.DAILY_WORD));
        handlers.put(EventType.GET_BRANCH_REPORT_JSON, new ReportHandler(reportService, ReportHandler.ReportType.BRANCH_JSON));
        handlers.put(EventType.GET_BRANCH_REPORT_WORD, new ReportHandler(reportService, ReportHandler.ReportType.BRANCH_WORD));
    }

    public SocketHandler createHandler(EventType eventType) {
        SocketHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("Invalid handler type: " + eventType);
        }
        return handler;
    }
}
