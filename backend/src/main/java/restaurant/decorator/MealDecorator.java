package restaurant.decorator;

import restaurant.meal.Meal;

/**
 * Decorator: wraps another Meal and implements the same interface.
 * By default it delegates everything to the wrapped meal;
 * concrete toppings add their own price and description on top.
 */
public abstract class MealDecorator implements Meal {
    protected final Meal meal;

    protected MealDecorator(Meal meal) {
        if (meal == null) {
            throw new IllegalArgumentException("Meal to decorate must not be null");
        }
        this.meal = meal;
    }

    @Override public String getName()        { return meal.getName(); }
    @Override public double getPrice()       { return meal.getPrice(); }
    @Override public String getDescription() { return meal.getDescription(); }
}
