package restaurant.meal;

/** Concrete product of the Factory. */
public class Drink implements Meal {
    @Override public String getName()        { return "Drink"; }
    @Override public double getPrice()       { return 600; }
    @Override public String getDescription() { return "Lemonade"; }
}
