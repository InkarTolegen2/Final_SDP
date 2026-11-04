package restaurant.meal;

import java.util.List;

/**
 * Factory: the only place that knows which class stands behind a meal type.
 * Clients (facade, REST API, main) ask for "pizza" and get a Meal,
 * they never call new Pizza() themselves.
 */
public final class MealFactory {

    /** Types the factory can create, used for the menu. */
    public static final List<String> TYPES = List.of("pizza", "burger", "drink");

    private MealFactory() { }

    public static Meal create(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Meal type must not be null");
        }
        switch (type.trim().toLowerCase()) {
            case "pizza":  return new Pizza();
            case "burger": return new Burger();
            case "drink":  return new Drink();
            default: throw new IllegalArgumentException("Unknown meal type: " + type);
        }
    }
}
