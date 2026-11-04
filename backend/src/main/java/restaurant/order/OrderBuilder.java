package restaurant.order;

import restaurant.meal.Meal;

import java.util.ArrayList;
import java.util.List;

/** Builder: collects dishes, table number and comment step by step, then build() validates. */
public class OrderBuilder {
    private final List<String> dishes = new ArrayList<>();
    private final List<Meal> meals = new ArrayList<>();
    private int tableNumber;
    private String comment = "";

    public OrderBuilder addDish(String dish) {
        if (dish == null || dish.isBlank()) {
            throw new IllegalArgumentException("Dish name must not be empty");
        }
        dishes.add(dish);
        return this;
    }

    /** Adds a real Meal (possibly decorated); its description goes to the dishes list. */
    public OrderBuilder addDish(Meal meal) {
        if (meal == null) {
            throw new IllegalArgumentException("Meal must not be null");
        }
        meals.add(meal);
        dishes.add(meal.getDescription());
        return this;
    }

    public OrderBuilder table(int tableNumber) {
        this.tableNumber = tableNumber;
        return this;
    }

    public OrderBuilder comment(String comment) {
        this.comment = comment == null ? "" : comment;
        return this;
    }

    public Order build() {
        if (dishes.isEmpty()) {
            throw new IllegalStateException("Order needs at least one dish");
        }
        if (tableNumber <= 0) {
            throw new IllegalStateException("Table number is required");
        }
        return new Order(dishes, meals, tableNumber, comment);
    }
}
