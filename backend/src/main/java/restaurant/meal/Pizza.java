package restaurant.meal;

/** Concrete product of the Factory. */
public class Pizza implements Meal {
    @Override public String getName()        { return "Pizza"; }
    @Override public double getPrice()       { return 2500; }
    @Override public String getDescription() { return "Pizza Margherita"; }
}
