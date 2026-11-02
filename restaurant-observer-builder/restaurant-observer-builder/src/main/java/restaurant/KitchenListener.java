package restaurant;

/** Observer for the kitchen display. */
public class KitchenListener implements OrderListener {
    @Override
    public void onStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.READY) {
            System.out.println("[Kitchen] Order#" + order.getId() + " is ready, remove from the board");
        } else {
            System.out.println("[Kitchen] Order#" + order.getId() + ": " + oldStatus + " -> " + newStatus
                    + ", dishes " + order.getDishes());
        }
    }
}
