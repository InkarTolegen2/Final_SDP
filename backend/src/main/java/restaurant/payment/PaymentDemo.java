package restaurant.payment;

public class PaymentDemo {
    public static void main(String[] args) {
        PaymentContext ctx = new PaymentContext();
        double total = 10000;
        
        PaymentStrategy [] strategies = {
            new NoDiscount(),
            new PercentDiscount(10),
            new PromoCodeDiscount("FIRST"),
            new PromoCodeDiscount("Wrong")
        };

        for (PaymentStrategy s : strategies) {
            ctx.setStrategy(s);
            System.out.println(s.getName() + " -> " + ctx.checkout(total));
        }
    }
}
