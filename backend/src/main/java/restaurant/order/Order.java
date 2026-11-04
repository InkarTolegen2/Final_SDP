package restaurant.order;

import restaurant.meal.Meal;
import restaurant.observer.OrderListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subject of the Observer pattern.
 * Created only through {@link OrderBuilder} (constructor is package-private).
 */
public class Order {
    private static int counter = 0;

    private final int id;
    private final List<String> dishes;
    private final List<Meal> meals;
    private final int tableNumber;
    private final String comment;
    private OrderStatus status = OrderStatus.NEW;
    private final List<OrderListener> listeners = new ArrayList<>();

    Order(List<String> dishes, List<Meal> meals, int tableNumber, String comment) {
        this.id = ++counter;
        this.dishes = Collections.unmodifiableList(new ArrayList<>(dishes));
        this.meals = Collections.unmodifiableList(new ArrayList<>(meals));
        this.tableNumber = tableNumber;
        this.comment = comment;
    }

    // ---------- Observer: subscribers ----------
    public void subscribe(OrderListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(OrderListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(OrderStatus oldStatus) {
        // iterate over a copy: a listener may unsubscribe while being notified
        for (OrderListener l : new ArrayList<>(listeners)) {
            l.onStatusChanged(this, oldStatus, status);
        }
    }

    // ---------- status change: only forward, one step at a time ----------
    public void setStatus(OrderStatus newStatus) {
        if (newStatus.ordinal() != status.ordinal() + 1) {
            throw new IllegalStateException(
                    "Order#" + id + ": " + status + " -> " + newStatus + " is not allowed");
        }
        OrderStatus old = status;
        status = newStatus;
        notifyListeners(old);
    }

    public int getId()              { return id; }
    public List<String> getDishes() { return dishes; }
    public List<Meal> getMeals()    { return meals; }

    /** Sum of Meal prices (dishes added only by name have no price). */
    public double getTotalPrice() {
        double sum = 0;
        for (Meal m : meals) sum += m.getPrice();
        return sum;
    }
    public int getTableNumber()     { return tableNumber; }
    public String getComment()      { return comment; }
    public OrderStatus getStatus()  { return status; }

    @Override
    public String toString() {
        return "Order#" + id + " [table " + tableNumber + ", dishes " + dishes
                + (comment.isEmpty() ? "" : ", comment \"" + comment + "\"") + "]";
    }
}
