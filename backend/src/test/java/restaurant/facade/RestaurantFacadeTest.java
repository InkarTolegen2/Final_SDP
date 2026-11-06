package restaurant.facade;

import org.junit.jupiter.api.Test;
import restaurant.facade.RestaurantFacade.ItemRequest;
import restaurant.facade.RestaurantFacade.OrderRequest;
import restaurant.facade.RestaurantFacade.PlacedOrder;
import restaurant.order.OrderStatus;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantFacadeTest {

    private final RestaurantFacade facade = new RestaurantFacade();

    private OrderRequest request(String discountType, String discountValue) {
        return new OrderRequest(5, "no onion", "card", discountType, discountValue, List.of(
                new ItemRequest("pizza", List.of("cheese", "large"), 2),   // (2500+300+500) * 2 = 6600
                new ItemRequest("drink", List.of(), 1)));                  // 600
    }

    @Test
    void menuIsBuiltFromFactoryAndDecorators() {
        RestaurantFacade.Menu menu = facade.menu();
        assertEquals(3, menu.meals().size());
        assertEquals(2500, menu.meals().get(0).price());
        assertEquals(3, menu.toppings().size());
        assertEquals("cheese", menu.toppings().get(0).id());
        assertEquals(300, menu.toppings().get(0).price());
        assertEquals("extra cheese", menu.toppings().get(0).label());
    }

    @Test
    void placeOrderUsesFactoryDecoratorBuilder() {
        PlacedOrder p = facade.placeOrder(request("none", null));
        assertEquals(7200, p.total());
        assertEquals(7200, p.finalTotal());
        assertEquals(5, p.order().getTableNumber());
        assertEquals(OrderStatus.NEW, p.order().getStatus());
        assertEquals("Pizza Margherita, extra cheese, large size", p.items().get(0).description());
        assertEquals(3300, p.items().get(0).unitPrice());
    }

    @Test
    void strategiesChangeTheTotal() {
        assertEquals(7200 * 0.9, facade.placeOrder(request("percent", "10")).finalTotal(), 1e-9);
        assertEquals(7200 * 0.85, facade.placeOrder(request("promo", "first")).finalTotal(), 1e-9);
        assertEquals(7200, facade.placeOrder(request("promo", "WRONG")).finalTotal());
    }

    @Test
    void statusGoesOnlyForward() {
        int id = facade.placeOrder(request("none", null)).order().getId();
        facade.changeStatus(id, "cooking");
        assertEquals(OrderStatus.COOKING, facade.order(id).order().getStatus());
        assertThrows(IllegalStateException.class, () -> facade.changeStatus(id, "NEW"));
        facade.changeStatus(id, "READY");
        assertEquals(OrderStatus.READY, facade.order(id).order().getStatus());
    }

    @Test
    void badRequestsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> facade.placeOrder(
                new OrderRequest(5, "", "card", "none", null, List.of())));
        assertThrows(IllegalStateException.class, () -> facade.placeOrder(
                new OrderRequest(0, "", "card", "none", null, List.of(new ItemRequest("pizza", List.of(), 1)))));
        assertThrows(IllegalArgumentException.class, () -> facade.placeOrder(
                new OrderRequest(5, "", "card", "none", null, List.of(new ItemRequest("sushi", List.of(), 1)))));
        assertThrows(IllegalArgumentException.class, () -> facade.placeOrder(
                new OrderRequest(5, "", "bitcoin", "none", null, List.of(new ItemRequest("pizza", List.of(), 1)))));
        assertThrows(NoSuchElementException.class, () -> facade.order(9999));
    }
}
