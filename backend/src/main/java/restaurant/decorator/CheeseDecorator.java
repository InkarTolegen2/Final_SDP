package restaurant.decorator;

import restaurant.meal.Meal;

/** Topping: extra cheese, +300. */
public class CheeseDecorator extends MealDecorator {
    public static final double PRICE = 300;

    public CheeseDecorator(Meal meal) { super(meal); }

    @Override public double getPrice()       { return meal.getPrice() + PRICE; }
    @Override public String getDescription() { return meal.getDescription() + ", extra cheese"; }
}
