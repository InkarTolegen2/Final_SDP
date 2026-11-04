package restaurant.payment;

public class PaymentContext {
    private PaymentStrategy strategy = new NoDiscount();

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public double checkout(double total) {
        return strategy.apply(total);
    }
}
