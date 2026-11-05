package restaurant.facade;

import restaurant.decorator.Toppings;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class RestaurantFacade {

    public record ItemRequest(String type, List<String> toppings, int qty) { }

    public record OrderRequest(int table, String comment, String payment,
                               String discountType, String discountValue, List<ItemRequest> items) { }

    public record LineItem(String type, String description, int qty, double unitPrice) { }

    public record PlacedOrder(Order order, String payment, String discountName,
                              double total, double finalTotal, long createdAt, List<LineItem> items) { }

    public record MealInfo(String type, String name, String description, double price) { }

    public record ToppingInfo(String id, String name, String label, double price) { }

    public record Menu(List<MealInfo> meals, List<ToppingInfo> toppings) { }

    private final Map<Integer, PlacedOrder> orders = new ConcurrentHashMap<>();

    public Menu menu() {
        List<MealInfo> meals = new ArrayList<>();
        for (String type : MealFactory.TYPES) {
            Meal m = MealFactory.create(type);
            meals.add(new MealInfo(type, m.getName(), m.getDescription(), m.getPrice()));
        }
        Meal probe = MealFactory.create(MealFactory.TYPES.get(0));
        List<ToppingInfo> toppings = new ArrayList<>();
        for (String id : Toppings.NAMES) {
            Meal decorated = Toppings.apply(probe, id);
            String label = decorated.getDescription().substring(probe.getDescription().length() + 2);
            String name = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            toppings.add(new ToppingInfo(id, name, label, decorated.getPrice() - probe.getPrice()));
        }
        return new Menu(meals, toppings);
    }

    public PlacedOrder placeOrder(OrderRequest req) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("Order needs at least one dish");
        }
        String payment = req.payment() == null ? "card" : req.payment().trim().toLowerCase();
        if (!payment.equals("card") && !payment.equals("cash")) {
            throw new IllegalArgumentException("Payment must be card or cash");
        }

        OrderBuilder builder = new OrderBuilder().table(req.table()).comment(req.comment());
        List<LineItem> lines = new ArrayList<>();
        for (ItemRequest it : req.items()) {
            if (it.qty() < 1 || it.qty() > 50) {
                throw new IllegalArgumentException("Quantity must be from 1 to 50");
            }
            Meal meal = Toppings.applyAll(MealFactory.create(it.type()), it.toppings());
            for (int i = 0; i < it.qty(); i++) {
                builder.addDish(meal);
            }
            lines.add(new LineItem(it.type(), meal.getDescription(), it.qty(), meal.getPrice()));
        }
        Order order = builder.build();

        order.subscribe(new KitchenListener());
        order.subscribe(new CustomerListener());

        PaymentStrategy strategy = strategyFor(req.discountType(), req.discountValue());
        PaymentContext payments = new PaymentContext();
        payments.setStrategy(strategy);
        double total = order.getTotalPrice();
        double finalTotal = payments.checkout(total);

        PlacedOrder placed = new PlacedOrder(order, payment, strategy.getName(),
                total, finalTotal, System.currentTimeMillis(), lines);
        orders.put(order.getId(), placed);
        return placed;
    }

    public List<PlacedOrder> orders() {
        List<PlacedOrder> list = new ArrayList<>(orders.values());
        list.sort(Comparator.comparingInt(p -> p.order().getId()));
        return list;
    }

    public PlacedOrder order(int id) {
        PlacedOrder p = orders.get(id);
        if (p == null) throw new NoSuchElementException("Order #" + id + " not found");
        return p;
    }

    public PlacedOrder changeStatus(int id, String status) {
        PlacedOrder p = order(id);
        if (status == null) throw new IllegalArgumentException("Status is required");
        OrderStatus next;
        try {
            next = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
        p.order().setStatus(next);
        return p;
    }

    private static PaymentStrategy strategyFor(String type, String value) {
        String t = type == null ? "none" : type.trim().toLowerCase();
        switch (t) {
            case "none":
                return new NoDiscount();
            case "percent":
                try {
                    double p = Double.parseDouble(value);
                    if (p < 0 || p > 100) throw new IllegalArgumentException("Percent must be from 0 to 100");
                    return new PercentDiscount(p);
                } catch (NumberFormatException | NullPointerException e) {
                    throw new IllegalArgumentException("Percent discount needs a number");
                }
            case "promo":
                return new PromoCodeDiscount(value == null ? "" : value.trim().toUpperCase());
            default:
                throw new IllegalArgumentException("Unknown discount type: " + type);
        }
    }
}
