package restaurant.payment;

public class PercentDiscount implements PaymentStrategy {
    private final double percent;

    public PercentDiscount(double percent) {
        this.percent = percent;
    }

    @Override 
    public double apply(double total) {
        return total - total * percent / 100;
    }

    @Override 
    public String getName() {
        return percent + "% discount";
    }
}
