package restaurant.order;

import restaurant.observer.CustomerListener;
import restaurant.observer.KitchenListener;

/** Console demo of Builder + Observer (person 2), was Main before the web server. */
public class OrderDemo {
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
