package restaurant.facade;

import restaurant.order.OrderStatus;

import java.util.List;

public class FacadeDemo {
    public static void main(String[] args) {
        RestaurantFacade facade = new RestaurantFacade();

        OrderRequest req = new OrderRequest(
                List.of(new OrderItem("pizza", List.of("cheese")),
                        new OrderItem("drink", List.of())),
                5, "no onions", "PROMO", "FIRST");

        OrderResult result = facade.placeOrder(req);
        System.out.println(result);

        facade.updateStatus(result.getOrderId(), OrderStatus.COOKING);
        facade.updateStatus(result.getOrderId(), OrderStatus.READY);
        System.out.println(facade.getStatus(result.getOrderId()));
    }
}