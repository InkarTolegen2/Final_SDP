# Restaurant Ordering System: Builder + Observer

Java 17, JUnit 5, Maven.

```
src/main/java/restaurant/   OrderStatus, OrderListener, Order, OrderBuilder,
                            KitchenListener, CustomerListener, Main
src/test/java/restaurant/   OrderObserverTest
report/                     Builder_Observer_report.md, observer_sequence.png / .mmd
```

Run tests: `mvn test`
Run demo:  `mvn compile exec:java -Dexec.mainClass=restaurant.Main`
(or run `restaurant.Main` from the IDE)
