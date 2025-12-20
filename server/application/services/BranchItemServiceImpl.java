package server.application.services;

import server.application.adaptors.BranchItemService;
import server.domain.Branch;
import server.domain.BranchInventoryItem;
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
            logRepository.info("Getting items for branch (BranchItemService): " + branchId);
            List<BranchInventoryItem> items = branchInventoryItemRepository.findByBranchId(branchId);
            return items;
        } catch (Exception e) {
            Error error = new Error(
                    "Get branch items error in BranchItemService, when trying to find items for branch: "
                            + branchId + ", " + e.getMessage());
            logRepository.error(error);
            return new ArrayList<>();
        }
    }

    @Override
    public BranchInventoryItem buyItem(UUID branchId, UUID itemId, UUID customerId, int quantity) {
        logRepository.info("Buying item, branchId=" + branchId + ", itemId=" + itemId
                + ", customerId=" + customerId + ", quantity=" + quantity);

        if (branchId == null || itemId == null || customerId == null) {
            Error error = new Error("Buy item failed, one of the ids is null");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (quantity <= 0) {
            Error error = new Error("Buy item failed, quantity must be > 0, got: " + quantity);
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        try {
            // Validate branch exists
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                Error error = new Error("Buy item failed, branch not found: " + branchId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            // Validate customer exists
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                Error error = new Error("Buy item failed, customer not found: " + customerId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            // Load item
            Optional<BranchInventoryItem> itemOpt = branchInventoryItemRepository.findById(itemId);
            if (itemOpt.isEmpty()) {
                Error error = new Error("Buy item failed, item not found: " + itemId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            BranchInventoryItem item = itemOpt.get();

            if (!item.getBranchId().equals(branchId)) {
                Error error = new Error("Buy item failed, item does not belong to branch, itemId: "
                        + itemId + ", branchId: " + branchId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            Customer customer = customerOpt.get();

            // Lock on both item and customer to ensure thread safety
            Object itemLock = getItemLock(itemId);
            Object customerLock = getCustomerLock(customerId);

            // Acquire locks in consistent order to avoid deadlock (always item first, then
            // customer)
            synchronized (itemLock) {
                synchronized (customerLock) {
                    // Reload item and customer to get latest state after acquiring locks
                    itemOpt = branchInventoryItemRepository.findById(itemId);
                    if (itemOpt.isEmpty()) {
                        Error error = new Error("Buy item failed, item not found after lock: " + itemId);
                        logRepository.error(error);
                        throw new IllegalArgumentException(error);
                    }
                    item = itemOpt.get();

                    customerOpt = customerRepository.findById(customerId);
                    if (customerOpt.isEmpty()) {
                        Error error = new Error("Buy item failed, customer not found after lock: " + customerId);
                        logRepository.error(error);
                        throw new IllegalArgumentException(error);
                    }
                    customer = customerOpt.get();

                    // Calculate original price and final price using customer's pricing strategy
                    double originalPrice = item.getUnitPrice() * quantity;
                    double finalPrice = customer.calculateFinalPrice(originalPrice);

                    // Update customer purchase history
                    customer.addPurchase(finalPrice);

                    // Update customer status based on total purchases
                    int totalPurchases = customer.getTotalPurchases();
                    if (totalPurchases >= 5 && customer.getCustomerType() != CustomerType.VIP) {
                        customer.setCustomerType(CustomerType.VIP);
                        logRepository.info("Customer upgraded to VIP, customerId=" + customerId
                                + ", totalPurchases=" + totalPurchases);
                    } else if (totalPurchases >= 3 && customer.getCustomerType() == CustomerType.NEW) {
                        customer.setCustomerType(CustomerType.RETURNING);
                        logRepository.info("Customer upgraded to RETURNING, customerId=" + customerId
                                + ", totalPurchases=" + totalPurchases);
                    }

                    // Update customer in repository
                    customerRepository.update(customer);

                    // Sell from inventory
                    item.sell(quantity);
                    branchInventoryItemRepository.update(item);

                    logRepository.info("Buy item succeeded, itemId=" + itemId + ", branchId=" + branchId
                            + ", customerId=" + customerId + ", quantity=" + quantity
                            + ", originalPrice=" + originalPrice + ", finalPrice=" + finalPrice
                            + ", remainingStock=" + item.getQuantityInStock()
                            + ", customerTotalPurchases=" + customer.getTotalPurchases()
                            + ", customerTotalSpent=" + customer.getTotalSpent()
                            + ", customerType=" + customer.getCustomerType());

                    return item;
                }
            }
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            Error error = new Error("Buy item error, itemId=" + itemId + ", branchId=" + branchId
                    + ", customerId=" + customerId + ", message=" + e.getMessage());
            logRepository.error(error);
            throw new RuntimeException(error);
        }
    }

    @Override
    public BranchInventoryItem restockItem(UUID branchId, UUID itemId, int quantity) {
        logRepository.info("Restocking item, branchId=" + branchId + ", itemId=" + itemId
                + ", quantity=" + quantity);

        if (branchId == null || itemId == null) {
            Error error = new Error("Restock item failed, branchId or itemId is null");
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        if (quantity <= 0) {
            Error error = new Error("Restock item failed, quantity must be > 0, got: " + quantity);
            logRepository.error(error);
            throw new IllegalArgumentException(error);
        }

        try {
            // Validate branch exists
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (branchOpt.isEmpty()) {
                Error error = new Error("Restock item failed, branch not found: " + branchId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            // Load item
            Optional<BranchInventoryItem> itemOpt = branchInventoryItemRepository.findById(itemId);
            if (itemOpt.isEmpty()) {
                Error error = new Error("Restock item failed, item not found: " + itemId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            BranchInventoryItem item = itemOpt.get();

            if (!item.getBranchId().equals(branchId)) {
                Error error = new Error("Restock item failed, item does not belong to branch, itemId: "
                        + itemId + ", branchId: " + branchId);
                logRepository.error(error);
                throw new IllegalArgumentException(error);
            }

            // Lock on item to ensure thread safety
            Object itemLock = getItemLock(itemId);
            synchronized (itemLock) {
                // Reload item to get latest state after acquiring lock
                itemOpt = branchInventoryItemRepository.findById(itemId);
                if (itemOpt.isEmpty()) {
                    Error error = new Error("Restock item failed, item not found after lock: " + itemId);
                    logRepository.error(error);
                    throw new IllegalArgumentException(error);
                }
                item = itemOpt.get();

                // Domain logic: restock inventory
                item.restock(quantity);

                branchInventoryItemRepository.update(item);

                logRepository.info("Restock item succeeded, itemId=" + itemId + ", branchId=" + branchId
                        + ", quantity=" + quantity + ", newStock=" + item.getQuantityInStock());

                return item;
            }
        } catch (IllegalArgumentException ex) {
            // Already logged above
            throw ex;
        } catch (Exception e) {
            Error error = new Error("Restock item error, itemId=" + itemId + ", branchId=" + branchId
                    + ", message=" + e.getMessage());
            logRepository.error(error);
            throw new RuntimeException(error);
        }
    }
}
