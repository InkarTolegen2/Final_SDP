package restaurant.meal;

import org.junit.jupiter.api.Test;
import restaurant.decorator.CheeseDecorator;
import restaurant.decorator.LargeSizeDecorator;
import restaurant.decorator.SauceDecorator;
import restaurant.decorator.Toppings;
import restaurant.order.Order;
import restaurant.order.OrderBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MealFactoryDecoratorTest {

    // ---------- Factory ----------
    @Test
    void factoryCreatesRightClasses() {
        assertInstanceOf(Pizza.class, MealFactory.create("pizza"));
        assertInstanceOf(Burger.class, MealFactory.create("Burger"));
        assertInstanceOf(Drink.class, MealFactory.create(" DRINK "));
    }

    @Test
    void factoryRejectsUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> MealFactory.create("sushi"));
        assertThrows(IllegalArgumentException.class, () -> MealFactory.create(null));
    }

    @Test
    void basePrices() {
        assertEquals(2500, MealFactory.create("pizza").getPrice());
        assertEquals(1800, MealFactory.create("burger").getPrice());
        assertEquals(600, MealFactory.create("drink").getPrice());
    }

    // ---------- Decorator ----------
    @Test
    void oneToppingAddsPriceAndDescription() {
        Meal m = new CheeseDecorator(MealFactory.create("pizza"));
        assertEquals(2800, m.getPrice());
        assertEquals("Pizza Margherita, extra cheese", m.getDescription());
        assertEquals("Pizza", m.getName());
    }

    @Test
    void allThreeToppingsStack() {
        Meal m = new LargeSizeDecorator(new SauceDecorator(new CheeseDecorator(MealFactory.create("pizza"))));
        assertEquals(2500 + 300 + 150 + 500, m.getPrice());
        assertEquals("Pizza Margherita, extra cheese, sauce, large size", m.getDescription());
    }

    @Test
    void sameToppingTwice() {
        Meal m = new CheeseDecorator(new CheeseDecorator(MealFactory.create("burger")));
        assertEquals(1800 + 600, m.getPrice());
    }

    @Test
    void toppingsByName() {
        Meal m = Toppings.applyAll(MealFactory.create("drink"), List.of("large", "sauce"));
        assertEquals(600 + 500 + 150, m.getPrice());
        assertThrows(IllegalArgumentException.class,
                () -> Toppings.apply(MealFactory.create("drink"), "ketchup"));
    }

    @Test
    void decoratedMealGoesIntoOrder() {
        Meal pizza = new CheeseDecorator(MealFactory.create("pizza"));
        Order order = new OrderBuilder()
                .addDish(pizza)
                .addDish(MealFactory.create("drink"))
                .table(3)
                .build();
        assertEquals(List.of("Pizza Margherita, extra cheese", "Lemonade"), order.getDishes());
        assertEquals(2800 + 600, order.getTotalPrice());
    }
}
