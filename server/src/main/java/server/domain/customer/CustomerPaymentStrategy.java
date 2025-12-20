package server.domain.customer;

public interface CustomerPaymentStrategy {
    double calculateFinalPrice(double originalPrice);

    double calculateDiscount(double originalPrice);

    CustomerType getCustomerType();
}
