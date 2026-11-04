package restaurant.facade;

import java.util.List;

public class OrderItem {
    private final String mealId;
    private final List<String> toppings;

    public OrderItem(String mealId, List<String> toppings) {
        this.mealId = mealId;
        this.toppings = toppings;
    }
    public String getMealId() {
        return mealId;
    }

    public List<String> getToppings() {
        return toppings;
    }
}
