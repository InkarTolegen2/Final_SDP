package restaurant.meal;

import restaurant.decorator.CheeseDecorator;
import restaurant.decorator.LargeSizeDecorator;
import restaurant.decorator.SauceDecorator;

/** Console check for Factory + Decorator (person 1). */
public class MealDemo {
    public static void main(String[] args) {
        System.out.println("=== Factory ===");
        for (String type : MealFactory.TYPES) {
            print(MealFactory.create(type));
        }

        System.out.println();
        System.out.println("=== Decorator ===");
        Meal pizza = MealFactory.create("pizza");
        print(pizza);

        pizza = new CheeseDecorator(pizza);
        print(pizza);

        pizza = new SauceDecorator(pizza);
        print(pizza);

        pizza = new LargeSizeDecorator(pizza);
        print(pizza);

        Meal burger = new CheeseDecorator(new CheeseDecorator(MealFactory.create("burger")));
        print(burger);

        Meal drink = new LargeSizeDecorator(MealFactory.create("drink"));
        print(drink);
    }

    private static void print(Meal m) {
        System.out.printf("%-7s | %-50s | %7.0f tg%n", m.getName(), m.getDescription(), m.getPrice());
    }
}
