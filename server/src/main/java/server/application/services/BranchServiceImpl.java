package server.application.services;

import server.application.adaptors.BranchService;
import server.domain.Branch;
import server.domain.BranchInventoryItem;
import server.domain.LogEntry;
import server.domain.employee.Employee;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import server.infustructre.adaptors.BranchRepository;
import server.infustructre.adaptors.EmployeeRepository;
import server.infustructre.adaptors.LogRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final BranchInventoryItemRepository branchInventoryItemRepository;
    private final EmployeeRepository employeeRepository;
    private final LogRepository logRepository;

    public BranchServiceImpl(BranchRepository branchRepository,
            BranchInventoryItemRepository branchInventoryItemRepository,
            EmployeeRepository employeeRepository,
            LogRepository logRepository) {
        this.branchRepository = branchRepository;
        this.branchInventoryItemRepository = branchInventoryItemRepository;
        this.employeeRepository = employeeRepository;
        this.logRepository = logRepository;
    }

    @Override
    public Optional<Branch> getBranch(UUID branchId) {
        try {
            logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET BRANCH] for branch: " + branchId);
            Optional<Branch> branch = branchRepository.findById(branchId);
            return branch;
        } catch (Exception e) {
            String errorMessage = "[GET BRANCH] failed, when trying to find branch: " + branchId + ", " + e.getMessage();
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            return Optional.empty();
        }
    }

    @Override
    public List<BranchInventoryItem> getBranchItems(UUID branchId) {
        try {
            logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET BRANCH ITEMS] for branch: " + branchId);
            List<BranchInventoryItem> items = branchInventoryItemRepository.findByBranchId(branchId);
            return items;
        } catch (Exception e) {
            String errorMessage = "[GET BRANCH ITEMS] failed, when trying to find items for branch: " + branchId + ", "
                    + e.getMessage();
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Employee> getBranchEmployees(UUID branchId) {
        try {
            logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET BRANCH EMPLOYEES] for branch: " + branchId);
            List<Employee> employees = employeeRepository.findByBranchId(branchId);
            logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET BRANCH EMPLOYEES] found " + employees.size() + " employees for branch: " + branchId);
            return employees;
        } catch (Exception e) {
            String errorMessage = "[GET BRANCH EMPLOYEES] failed, when trying to find employees for branch: " + branchId
                    + ", " + e.getMessage();
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            return new ArrayList<>();
        }
    }
}
