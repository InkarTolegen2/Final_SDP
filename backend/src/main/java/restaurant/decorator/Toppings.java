package restaurant.decorator;

import restaurant.meal.Meal;

import java.util.List;

/**
 * Helper for the facade / REST API: applies toppings given by name,
 * e.g. from the JSON {"type":"pizza","toppings":["cheese","large"]}.
 */
public final class Toppings {

    public static final List<String> NAMES = List.of("cheese", "sauce", "large");

    private Toppings() { }

    public static Meal apply(Meal meal, String topping) {
        if (topping == null) {
            throw new IllegalArgumentException("Topping must not be null");
        }
        switch (topping.trim().toLowerCase()) {
            case "cheese": return new CheeseDecorator(meal);
            case "sauce":  return new SauceDecorator(meal);
            case "large":  return new LargeSizeDecorator(meal);
            default: throw new IllegalArgumentException("Unknown topping: " + topping);
        }
    }

    public static Meal applyAll(Meal meal, List<String> toppings) {
        Meal result = meal;
        if (toppings != null) {
            for (String t : toppings) {
                result = apply(result, t);
            }
        }
        return result;
    }
}
