package restaurant.meal;

/**
 * Common interface for every dish (agreed by the whole team).
 * Concrete dishes and all decorators implement it, so the rest of the
 * system (order, cart, payment) works with any Meal the same way.
 */
public interface Meal {
    String getName();
    double getPrice();
    String getDescription();
}
