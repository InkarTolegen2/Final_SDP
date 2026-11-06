# Restaurant Ordering System

Java 17, Maven, JUnit 5. Web client: HTML/CSS/JS.
Backend: REST API on the JDK built-in HTTP server (`com.sun.net.httpserver`) with a small own JSON parser, so no external libraries are needed.

```
backend/
  src/main/java/restaurant/
    Main.java            starts the web server on http://localhost:7070
    meal/                Meal, Pizza, Burger, Drink, MealFactory, MealDemo        (person 1)
    decorator/           MealDecorator, Cheese/Sauce/LargeSizeDecorator, Toppings (person 1)
    order/               Order, OrderBuilder, OrderStatus, OrderDemo              (person 2)
    observer/            OrderListener, KitchenListener, CustomerListener         (person 2)
    payment/             PaymentStrategy, NoDiscount, PercentDiscount,
                         PromoCodeDiscount, PaymentContext, PaymentDemo           (person 3)
    facade/              RestaurantFacade                                         (person 3)
    api/                 Routes (REST endpoints + static files), Json             (person 3)
  src/test/java/restaurant/
    meal/MealFactoryDecoratorTest.java
    order/OrderObserverTest.java
    facade/RestaurantFacadeTest.java
    api/JsonTest.java, api/RoutesTest.java   (starts the server and calls the API over HTTP)
frontend/
  index.html    + js/menu.js      menu, toppings, price recalculation
  cart.html     + js/cart.js      cart, table/comment, discount (Strategy), card/cash, POST /order
  status.html   + js/status.js    order status NEW -> COOKING -> READY, polling every 2 s
  kitchen.html  + js/kitchen.js   kitchen board, moves orders forward, polling every 3 s
  js/api.js                       REST calls + demo mode in localStorage when the backend is offline
  css/style.css
docs/
  factory-decorator/   report, UML, screenshots (person 1)
  builder-observer/    report, sequence diagram (person 2)
  screenshots/         cart, status, kitchen pages
```

## Run

**Web app (backend + frontend):** run `restaurant.Main` (green ▶ in IntelliJ, or `cd backend` → `mvn compile exec:java`),
then open http://localhost:7070. Kitchen panel: http://localhost:7070/kitchen.html
(open it in a second tab, move the order forward and watch the status page and the server console).

**Tests and console demos:**

```
cd backend
mvn test
mvn compile exec:java -Dexec.mainClass=restaurant.meal.MealDemo        # Factory + Decorator
mvn compile exec:java -Dexec.mainClass=restaurant.order.OrderDemo      # Builder + Observer
mvn compile exec:java -Dexec.mainClass=restaurant.payment.PaymentDemo  # Strategy
```

**Frontend only (demo mode, no Java):** `python -m http.server` inside `frontend/`, open http://localhost:8000.
Orders are then kept in the browser's localStorage.

## Contracts for the team

**REST API (person 3):**
- `GET /menu` → `{ "meals": [{type, name, description, price}], "toppings": [{id, name, label, price}] }`
  — build it from `MealFactory.TYPES` + `MealFactory.create(type)`, and `Toppings.NAMES`
  (prices: `CheeseDecorator.PRICE`, `SauceDecorator.PRICE`, `LargeSizeDecorator.PRICE`).
- `POST /order` body:
  `{ table, comment, payment: "card"|"cash", discount: {type: "none"|"percent"|"promo", value}, items: [{type, toppings: ["cheese","large"], qty}] }`
  → `{ id, status, total, finalTotal, discountName }`.
  In the facade: `Toppings.applyAll(MealFactory.create(type), toppings)` → `builder.addDish(meal)` (qty times);
  discount `none` → `NoDiscount`, `percent` → `PercentDiscount(value)`, `promo` → `PromoCodeDiscount(value)`.
- `GET /order/{id}/status` → `{ id, status }`
- `GET /orders` → list of orders `{ id, table, comment, payment, items: [{type, description, qty, unitPrice}], total, finalTotal, discountName, status, createdAt }` (kitchen + status page)
- `POST /order/{id}/status` body `{ status }` → calls `order.setStatus(...)`, which notifies the listeners; 400 if the step is not allowed.

The frontend copies the discount rules (10 %, promo FIRST = 15 %) in `js/api.js` → keep them in sync with `restaurant.payment`.

**Cart in localStorage (person 2, cart.html):** key `cart`, array of
`{ type, name, description, toppings: [id...], unitPrice, qty }`.
Helpers `getCart()`, `saveCart()`, `formatPrice()` are in `js/api.js`.

## Changes to person 2's code

- Classes moved into packages `restaurant.order` and `restaurant.observer` (only `package`/`import` lines changed).
- `OrderBuilder.addDish(Meal)` added (the old `addDish(String)` is kept); `Order` got `getMeals()` and `getTotalPrice()`.
- All 5 original tests still pass.

Person 3's `payment` package is copied without changes.
