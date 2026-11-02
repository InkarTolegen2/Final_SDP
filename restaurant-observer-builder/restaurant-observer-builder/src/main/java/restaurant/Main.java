package restaurant;

public class Main {
    public static void main(String[] args) {
        Order order = new OrderBuilder()
                .addDish("Pasta")
                .addDish("Tea")
                .table(5)
                .comment("no onion")
                .build();

        order.subscribe(new KitchenListener());
        order.subscribe(new CustomerListener());

        System.out.println(order);
        order.setStatus(OrderStatus.COOKING);
        order.setStatus(OrderStatus.READY);
    }
}
