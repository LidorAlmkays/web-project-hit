package server.domain.customer;

public class VIPCustomerStrategy implements CustomerPaymentStrategy {
    private static final double VIP_DISCOUNT_PERCENTAGE = 0.15; // 15% discount
    private static final double VIP_FIXED_DISCOUNT = 50.0; // fixed discount
    private static final double MINIMUM_PURCHASE_FOR_FIXED_DISCOUNT = 500.0; // min purchase amount

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
        double percentageDiscount = originalPrice * VIP_DISCOUNT_PERCENTAGE;

        // Plus fixed discount
        double fixedDiscount = 0.0;
        if (originalPrice >= MINIMUM_PURCHASE_FOR_FIXED_DISCOUNT) {
            fixedDiscount = VIP_FIXED_DISCOUNT;
        }

        return percentageDiscount + fixedDiscount;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.VIP;
    }
}
