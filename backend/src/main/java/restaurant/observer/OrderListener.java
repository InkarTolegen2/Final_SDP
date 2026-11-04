package restaurant.observer;

import restaurant.order.Order;
import restaurant.order.OrderStatus;

/** Observer: anyone who wants to be told when an order changes its status. */
public interface OrderListener {
    void onStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}
