package restaurant.payment;

public class NoDiscount implements PaymentStrategy {
    @Override
    public double apply(double total) {
        return total;
    }

    @Override
    public String getName() {
        return "No Discount";
    }
}
