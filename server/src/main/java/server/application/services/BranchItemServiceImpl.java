package server.application.services;

import server.application.adaptors.BranchItemService;
import server.domain.Branch;
import server.domain.BranchInventoryItem;
import server.domain.LogEntry;
import server.domain.customer.Customer;
import server.domain.customer.CustomerType;
import server.infustructre.adaptors.BranchInventoryItemRepository;
import server.infustructre.adaptors.BranchRepository;
import server.infustructre.adaptors.CustomerRepository;
import server.infustructre.adaptors.LogRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BranchItemServiceImpl implements BranchItemService {

    private final BranchRepository branchRepository;
    private final BranchInventoryItemRepository branchInventoryItemRepository;
    private final CustomerRepository customerRepository;
    private final LogRepository logRepository;
    private final Map<UUID, Object> itemLocks = new ConcurrentHashMap<>();
    private final Map<UUID, Object> customerLocks = new ConcurrentHashMap<>();
    private final Object creationMutex = new Object();

    public BranchItemServiceImpl(BranchRepository branchRepository,
            BranchInventoryItemRepository branchInventoryItemRepository,
            CustomerRepository customerRepository,
            LogRepository logRepository) {
        this.branchRepository = branchRepository;
        this.branchInventoryItemRepository = branchInventoryItemRepository;
        this.customerRepository = customerRepository;
        this.logRepository = logRepository;
    }

    private Object getItemLock(UUID itemId) {
        Object lock = itemLocks.get(itemId);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = itemLocks.get(itemId);
                if (lock == null) {
                    lock = new Object();
                    itemLocks.put(itemId, lock);
                }
            }
        }
        return lock;
    }

    private Object getCustomerLock(UUID customerId) {
        Object lock = customerLocks.get(customerId);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = customerLocks.get(customerId);
                if (lock == null) {
                    lock = new Object();
                    customerLocks.put(customerId, lock);
                }
            }
        }
        return lock;
    }

    @Override
    public List<BranchInventoryItem> getBranchItems(UUID branchId) {
        try {
            logRepository.info(LogEntry.LogType.MANAGEMENT, "[GET BRANCH ITEMS] for branch: " + branchId);
            List<BranchInventoryItem> items = branchInventoryItemRepository.findByBranchId(branchId);
            return items;
        } catch (Exception e) {
            String errorMessage = "[GET BRANCH ITEMS] failed, when trying to find items for branch: "
                    + branchId + ", " + e.getMessage();
            logRepository.error(LogEntry.LogType.MANAGEMENT, errorMessage);
            return new ArrayList<>();
        }
    }

    @Override
    public BranchInventoryItem buyItem(UUID branchId, UUID itemId, UUID customerId, int quantity) {
        logRepository.info(LogEntry.LogType.PURCHASE, "[BUY ITEM] buying item, branchId=" + branchId + ", itemId=" + itemId
                + ", customerId=" + customerId + ", quantity=" + quantity);

        if (branchId == null || itemId == null || customerId == null) {
            Error error = new Error("[BUY ITEM] failed, one of the ids is null");
            logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (quantity <= 0) {
            Error error = new Error("[BUY ITEM] failed, quantity must be > 0, got: " + quantity);
            logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                Error error = new Error("[BUY ITEM] failed, branch not found: " + branchId);
                logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                Error error = new Error("[BUY ITEM] failed, customer not found: " + customerId);
                logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            Optional<BranchInventoryItem> itemOpt = branchInventoryItemRepository.findById(itemId);
            if (itemOpt.isEmpty()) {
                Error error = new Error("[BUY ITEM] failed, item not found: " + itemId);
                logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            BranchInventoryItem item = itemOpt.get();

            if (!item.getBranchId().equals(branchId)) {
                Error error = new Error("[BUY ITEM] failed, item does not belong to branch, itemId: "
                        + itemId + ", branchId: " + branchId);
                logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            Customer customer = customerOpt.get();

            Object itemLock = getItemLock(itemId);
            Object customerLock = getCustomerLock(customerId);

            synchronized (itemLock) {
                synchronized (customerLock) {
                    itemOpt = branchInventoryItemRepository.findById(itemId);
                    if (itemOpt.isEmpty()) {
                        Error error = new Error("[BUY ITEM] failed, item not found after lock: " + itemId);
                        logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                        throw new IllegalArgumentException(error);
                    }
                    item = itemOpt.get();

                    customerOpt = customerRepository.findById(customerId);
                    if (customerOpt.isEmpty()) {
                        Error error = new Error("[BUY ITEM] failed, customer not found after lock: " + customerId);
                        logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                        throw new IllegalArgumentException(error);
                    }
                    customer = customerOpt.get();

                    double originalPrice = item.getUnitPrice() * quantity;
                    double finalPrice = customer.calculateFinalPrice(originalPrice);

                    customer.addPurchase(finalPrice);

                    int totalPurchases = customer.getTotalPurchases();
                    if (totalPurchases >= 5 && customer.getCustomerType() != CustomerType.VIP) {
                        customer.setCustomerType(CustomerType.VIP);
                        logRepository.info(LogEntry.LogType.MANAGEMENT, "[CUSTOMER STATUS] customer upgraded to VIP, customerId=" + customerId
                                + ", totalPurchases=" + totalPurchases);
                    } else if (totalPurchases >= 3 && customer.getCustomerType() == CustomerType.NEW) {
                        customer.setCustomerType(CustomerType.RETURNING);
                        logRepository.info(LogEntry.LogType.MANAGEMENT, "[CUSTOMER STATUS] customer upgraded to RETURNING, customerId=" + customerId
                                + ", totalPurchases=" + totalPurchases);
                    }

                    customerRepository.update(customer);

                    item.sell(quantity);
                    branchInventoryItemRepository.update(item);

                    branchOpt = branchRepository.findById(branchId);
                    if (branchOpt.isEmpty()) {
                        Error error = new Error("[BUY ITEM] failed, branch not found after sale: " + branchId);
                        logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
                        throw new IllegalArgumentException(error);
                    }
                    Branch branch = branchOpt.get();
                    branch.addSale(quantity, finalPrice);
                    branchRepository.update(branch);

                    logRepository.info(LogEntry.LogType.PURCHASE, "[BUY ITEM] succeeded, itemId=" + itemId + ", branchId=" + branchId
                            + ", customerId=" + customerId + ", quantity=" + quantity
                            + ", originalPrice=" + originalPrice + ", finalPrice=" + finalPrice
                            + ", remainingStock=" + item.getQuantityInStock()
                            + ", customerTotalPurchases=" + customer.getTotalPurchases()
                            + ", customerTotalSpent=" + customer.getTotalSpent()
                            + ", customerType=" + customer.getCustomerType()
                            + ", branchTotalSold=" + branch.getTotalSold()
                            + ", branchTotalMoneyEarned=" + branch.getTotalMoneyEarned());

                    return item;
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            Error error = new Error("[BUY ITEM] error, itemId=" + itemId + ", branchId=" + branchId
                    + ", customerId=" + customerId + ", message=" + e.getMessage());
            logRepository.error(LogEntry.LogType.PURCHASE, error.getMessage());
            throw new RuntimeException(error);
        }
    }

    @Override
    public BranchInventoryItem restockItem(UUID branchId, UUID itemId, int quantity) {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "[RESTOCK ITEM] restocking item, branchId=" + branchId + ", itemId=" + itemId
                + ", quantity=" + quantity);

        if (branchId == null || itemId == null) {
            Error error = new Error("[RESTOCK ITEM] failed, branchId or itemId is null");
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (quantity <= 0) {
            Error error = new Error("[RESTOCK ITEM] failed, quantity must be > 0, got: " + quantity);
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                Error error = new Error("[RESTOCK ITEM] failed, branch not found: " + branchId);
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            Optional<BranchInventoryItem> itemOpt = branchInventoryItemRepository.findById(itemId);
            if (itemOpt.isEmpty()) {
                Error error = new Error("[RESTOCK ITEM] failed, item not found: " + itemId);
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            BranchInventoryItem item = itemOpt.get();

            if (!item.getBranchId().equals(branchId)) {
                Error error = new Error("[RESTOCK ITEM] failed, item does not belong to branch, itemId: "
                        + itemId + ", branchId: " + branchId);
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            Object itemLock = getItemLock(itemId);
            synchronized (itemLock) {
                itemOpt = branchInventoryItemRepository.findById(itemId);
                if (itemOpt.isEmpty()) {
                    Error error = new Error("[RESTOCK ITEM] failed, item not found after lock: " + itemId);
                    logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                    throw new IllegalArgumentException(error);
                }
                item = itemOpt.get();

                item.restock(quantity);

                branchInventoryItemRepository.update(item);

                logRepository.info(LogEntry.LogType.MANAGEMENT, "[RESTOCK ITEM] succeeded, itemId=" + itemId + ", branchId=" + branchId
                        + ", quantity=" + quantity + ", newStock=" + item.getQuantityInStock());

                return item;
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            Error error = new Error("[RESTOCK ITEM] error, itemId=" + itemId + ", branchId=" + branchId
                    + ", message=" + e.getMessage());
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new RuntimeException(error);
        }
    }

    @Override
    public BranchInventoryItem addItem(UUID branchId, String productName, String category, double unitPrice,
            int initialQuantity) {
        logRepository.info(LogEntry.LogType.MANAGEMENT, "[ADD ITEM] adding new item, branchId=" + branchId + ", productName=" + productName
                + ", category=" + category + ", unitPrice=" + unitPrice + ", initialQuantity=" + initialQuantity);

        if (branchId == null) {
            Error error = new Error("[ADD ITEM] failed, branchId is null");
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (productName == null || productName.trim().isEmpty()) {
            Error error = new Error("[ADD ITEM] failed, productName must not be null or empty");
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (category == null || category.trim().isEmpty()) {
            Error error = new Error("[ADD ITEM] failed, category must not be null or empty");
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (unitPrice < 0) {
            Error error = new Error("[ADD ITEM] failed, unitPrice must be non-negative, got: " + unitPrice);
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        if (initialQuantity < 0) {
            Error error = new Error("[ADD ITEM] failed, initialQuantity must be non-negative, got: " + initialQuantity);
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new IllegalArgumentException(error);
        }

        try {
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                Error error = new Error("[ADD ITEM] failed, branch not found: " + branchId);
                logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
                throw new IllegalArgumentException(error);
            }

            BranchInventoryItem newItem = new BranchInventoryItem(branchId, productName, category, unitPrice);

            if (initialQuantity > 0) {
                newItem.restock(initialQuantity);
            }

            branchInventoryItemRepository.save(newItem);

            logRepository.info(LogEntry.LogType.MANAGEMENT, "[ADD ITEM] succeeded, itemId=" + newItem.getItemId() + ", branchId=" + branchId
                    + ", productName=" + productName + ", category=" + category + ", unitPrice=" + unitPrice
                    + ", initialQuantity=" + initialQuantity + ", quantityInStock=" + newItem.getQuantityInStock());

            return newItem;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            Error error = new Error("[ADD ITEM] error, branchId=" + branchId + ", productName=" + productName
                    + ", message=" + e.getMessage());
            logRepository.error(LogEntry.LogType.MANAGEMENT, error.getMessage());
            throw new RuntimeException(error);
        }
    }
}
