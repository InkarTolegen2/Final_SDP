package restaurant;

/** Lifecycle of an order. Allowed transitions: NEW -> COOKING -> READY. */
public enum OrderStatus {
    NEW, COOKING, READY
}
