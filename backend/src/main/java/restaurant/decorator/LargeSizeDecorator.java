package restaurant.decorator;

import restaurant.meal.Meal;

/** Topping: large size, +500. */
public class LargeSizeDecorator extends MealDecorator {
    public static final double PRICE = 500;

    public LargeSizeDecorator(Meal meal) { super(meal); }

    @Override public double getPrice()       { return meal.getPrice() + PRICE; }
    @Override public String getDescription() { return meal.getDescription() + ", large size"; }
}
