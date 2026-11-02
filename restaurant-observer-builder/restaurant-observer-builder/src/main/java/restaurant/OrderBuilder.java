package restaurant;

import java.util.ArrayList;
import java.util.List;

/** Builder: collects dishes, table number and comment step by step, then build() validates. */
public class OrderBuilder {
    private final List<String> dishes = new ArrayList<>();
    private int tableNumber;
    private String comment = "";

    public OrderBuilder addDish(String dish) {
        if (dish == null || dish.isBlank()) {
            throw new IllegalArgumentException("Dish name must not be empty");
        }
        dishes.add(dish);
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
        return new Order(dishes, tableNumber, comment);
    }
}
