package restaurant.decorator;

import restaurant.meal.Meal;

/** Topping: sauce, +150. */
public class SauceDecorator extends MealDecorator {
    public static final double PRICE = 150;

    public SauceDecorator(Meal meal) { super(meal); }

    @Override public double getPrice()       { return meal.getPrice() + PRICE; }
    @Override public String getDescription() { return meal.getDescription() + ", sauce"; }
}
