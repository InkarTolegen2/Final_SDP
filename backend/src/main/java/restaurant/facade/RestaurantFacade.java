package restaurant.facade;

import restaurant.decorator.CheeseDecorator;
import restaurant.decorator.LargeSizeDecorator;
import restaurant.decorator.SauceDecorator;
import restaurant.meal.Meal;
import restaurant.meal.MealFactory;
import restaurant.observer.CustomerListener;
import restaurant.observer.KitchenListener;
import restaurant.order.Order;
import restaurant.order.OrderBuilder;
import restaurant.order.OrderStatus;
import restaurant.payment.NoDiscount;
import restaurant.payment.PaymentContext;
import restaurant.payment.PaymentStrategy;
import restaurant.payment.PercentDiscount;
import restaurant.payment.PromoCodeDiscount;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestaurantFacade {
    private final Map<Integer, Order> orders = new HashMap<>();
    private final PaymentContext payment = new PaymentContext();

    public Map<String, Meal> getMenu() {
        Map<String, Meal> menu = new LinkedHashMap<>();
        for (String type : MealFactory.TYPES) {
            menu.put(type, MealFactory.create(type));
        }
        return menu;
    }

    public Map<String, Double> getToppings() {
        Map<String, Double> toppings = new LinkedHashMap<>();
        toppings.put("cheese", CheeseDecorator.PRICE);
        toppings.put("sauce", SauceDecorator.PRICE);
        toppings.put("large", LargeSizeDecorator.PRICE);
        return toppings;
    }

    public OrderResult placeOrder(OrderRequest req) {
        OrderBuilder builder = new OrderBuilder()
                .table(req.getTable())
                .comment(req.getComment());

        for (OrderItem item : req.getItems()) {
            Meal meal = MealFactory.create(item.getMealId());      
            for (String topping : item.getToppings()) {
                meal = decorate(meal, topping);                   
            }
            builder.addDish(meal);                                
        }

        Order order = builder.build();
        order.subscribe(new KitchenListener());                   
        order.subscribe(new CustomerListener());

        payment.setStrategy(chooseStrategy(req));                 
        double finalTotal = payment.checkout(order.getTotalPrice());

        orders.put(order.getId(), order);
        return new OrderResult(order.getId(), finalTotal, order.getStatus().name());
    }

    public String getStatus(int orderId) {
        return find(orderId).getStatus().name();
    }

    public void updateStatus(int orderId, OrderStatus next) {
        find(orderId).setStatus(next);
    }

    private Meal decorate(Meal meal, String topping) {
        return switch (topping) {
            case "cheese" -> new CheeseDecorator(meal);
            case "sauce" -> new SauceDecorator(meal);
            case "large" -> new LargeSizeDecorator(meal);
            default -> throw new IllegalArgumentException("Unknown topping: " + topping);
        };
    }

    private Order find(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("No such order: " + orderId);
        }
        return order;
    }

    private PaymentStrategy chooseStrategy(OrderRequest req) {
        String d = req.getDiscount() == null ? "NONE" : req.getDiscount();
        return switch (d) {
            case "PERCENT" -> new PercentDiscount(10);
            case "PROMO" -> new PromoCodeDiscount(req.getPromoCode());
            default -> new NoDiscount();
        };
    }
}