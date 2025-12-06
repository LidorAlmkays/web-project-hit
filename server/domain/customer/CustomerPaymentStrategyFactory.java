package server.domain.customer;

public class CustomerPaymentStrategyFactory {
    public static CustomerPaymentStrategy createStrategy(CustomerType customerType) {
        if (customerType == null) {
            throw new IllegalArgumentException("customerType must not be null");
        }

        switch (customerType) {
            case NEW:
                return new NewCustomerStrategy();
            case RETURNING:
                return new ReturningCustomerStrategy();
            case VIP:
                return new VIPCustomerStrategy();
            default:
                throw new IllegalArgumentException("Unknown customer type: " + customerType);
        }
    }
}
