package restaurant.facade;

public class OrderResult {
    private final int orderId;
    private final double total;
    private final String status;

    public OrderResult(int orderId, double total, String status) {
        this.orderId = orderId;
        this.total = total;
        this.status = status;
    }

    public int getOrderId() { return orderId; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "Order#" + orderId + " total=" + total + " status=" + status;
    }
}