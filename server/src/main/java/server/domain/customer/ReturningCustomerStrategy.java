package server.domain.customer;

public class ReturningCustomerStrategy implements CustomerPaymentStrategy {
    private static final double RETURNING_CUSTOMER_DISCOUNT_PERCENTAGE = 0.10; // 10% discount
    private static final double MINIMUM_PURCHASE_FOR_DISCOUNT = 100.0; // min purchase amount

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
        // above minimum
        if (originalPrice >= MINIMUM_PURCHASE_FOR_DISCOUNT) {
            return originalPrice * RETURNING_CUSTOMER_DISCOUNT_PERCENTAGE;
        }
        return 0.0;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.RETURNING;
    }
}
