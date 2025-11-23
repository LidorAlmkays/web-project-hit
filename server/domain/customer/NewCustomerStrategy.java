package server.domain.customer;

public class NewCustomerStrategy implements CustomerPaymentStrategy {
    private static final double NEW_CUSTOMER_DISCOUNT_PERCENTAGE = 0.05; // 5% discount

    @Override
    public double calculateFinalPrice(double originalPrice) {
        if (originalPrice < 0) {
            throw new IllegalArgumentException("originalPrice must be non-negative");
        }
        double discount = calculateDiscount(originalPrice);
        return originalPrice - discount;
    }

    @Override
    public double calculateDiscount(double originalPrice) {
        if (originalPrice < 0) {
            throw new IllegalArgumentException("originalPrice must be non-negative");
        }
        return originalPrice * NEW_CUSTOMER_DISCOUNT_PERCENTAGE;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.NEW;
    }
}
