package restaurant.meal;

/** Concrete product of the Factory. */
public class Burger implements Meal {
    @Override public String getName()        { return "Burger"; }
    @Override public double getPrice()       { return 1800; }
    @Override public String getDescription() { return "Beef burger"; }
}
