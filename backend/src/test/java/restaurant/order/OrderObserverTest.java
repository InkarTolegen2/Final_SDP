package restaurant.order;

import restaurant.observer.OrderListener;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderObserverTest {

    private Order newOrder() {
        return new OrderBuilder().addDish("Pasta").addDish("Tea").table(5).comment("no onion").build();
    }

    @Test
    void builderCreatesOrderWithAllFields() {
        Order o = newOrder();
        assertEquals(List.of("Pasta", "Tea"), o.getDishes());
        assertEquals(5, o.getTableNumber());
        assertEquals("no onion", o.getComment());
        assertEquals(OrderStatus.NEW, o.getStatus());
    }

    @Test
    void builderRequiresDishAndTable() {
        assertThrows(IllegalStateException.class, () -> new OrderBuilder().table(1).build());
        assertThrows(IllegalStateException.class, () -> new OrderBuilder().addDish("Soup").build());
    }

    @Test
    void bothListenersReceiveEveryStatusChange() {
        Order order = newOrder();
        List<String> kitchenLog = new ArrayList<>();
        List<String> customerLog = new ArrayList<>();
        order.subscribe((o, oldS, newS) -> kitchenLog.add(oldS + "->" + newS));
        order.subscribe((o, oldS, newS) -> customerLog.add(oldS + "->" + newS));

        order.setStatus(OrderStatus.COOKING);
        order.setStatus(OrderStatus.READY);

        List<String> expected = List.of("NEW->COOKING", "COOKING->READY");
        assertEquals(expected, kitchenLog);
        assertEquals(expected, customerLog);
    }

    @Test
    void unsubscribedListenerGetsNothing() {
        Order order = newOrder();
        List<String> log = new ArrayList<>();
        OrderListener l = (o, a, b) -> log.add("x");
        order.subscribe(l);
        order.unsubscribe(l);
        order.setStatus(OrderStatus.COOKING);
        assertTrue(log.isEmpty());
    }

    @Test
    void cannotSkipOrGoBack() {
        Order order = newOrder();
        assertThrows(IllegalStateException.class, () -> order.setStatus(OrderStatus.READY));
        order.setStatus(OrderStatus.COOKING);
        assertThrows(IllegalStateException.class, () -> order.setStatus(OrderStatus.NEW));
    }
}
