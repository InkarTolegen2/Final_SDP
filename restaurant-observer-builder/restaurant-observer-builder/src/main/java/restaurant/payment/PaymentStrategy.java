package restaurant.payment;

public interface PaymentStrategy {
    double apply(double total);
    String getName();
}
