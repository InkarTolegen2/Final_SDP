package restaurant.payment;

public class PromoCodeDiscount implements PaymentStrategy {
    private final String code;

    public PromoCodeDiscount(String code) {
        this.code = code;
    }

    @Override
    public double apply(double total) {
        if ("FIRST".equalsIgnoreCase(code)) {
            return total * 0.85;
        }
        return total;
    }    

    @Override 
    public String getName() {
        return "Promo code: " + code;
    }
}
