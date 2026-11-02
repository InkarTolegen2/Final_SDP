package restaurant;

/** Observer that informs the customer (table) about the order status. */
public class CustomerListener implements OrderListener {
    @Override
    public void onStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println("[Customer, table " + order.getTableNumber() + "] Your order is now " + newStatus);
    }
}
