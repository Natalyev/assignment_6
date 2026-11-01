# Assignment 6 — Food Delivery Observer and Visitor

Variant 14, Food delivery. Java 17, JUnit 5, no runtime libraries beyond the JDK.

## Run

```text
mvn test
java -cp target/classes delivery.Main 4
```

The CLI argument sets the producer thread count. The demo sends four batches of 1,000 events and prints the exact count.

## Design

**Observer:** `Subject<T>` and `Observer<T>` are generic contracts. `OrderSource` and `CourierSource` are independent sources. `CustomerApp`, `RestaurantPanel`, `CourierApp`, `EtaTracker`, `Aggregator`, and `InspectionListener` are listeners. `Aggregator` is both a listener and a subject; it republishes counts to its own subscribers. `Event<T>` and all payloads are immutable records. Subscriptions are by `EventType`. `CopyOnWriteArrayList` gives each publication a stable listener snapshot, so a handler can subscribe or unsubscribe during notification. `ConcurrentHashMap` and `LongAdder` protect shared state. We chose **push**: the event carries a complete immutable snapshot, so listeners do not need to query a changing source.

**Visitor:** The stable tree is City → Restaurant → Menu → leaves. City, Restaurant, and Menu are containers; Dish, Combo, Drink, and Dessert are leaves. Each element calls `visitor.visit(this)` for double dispatch. Visitors own traversal, which lets a visitor choose whether and how to visit children. `CaloriesVisitor` calculates a number; `AllergenVisitor` accumulates state across the tree; `LowCalorieVisitor` filters leaves; `JsonVisitor` exports nested JSON with escaping. Elements contain data and dispatch only.

**Integration:** When an order reaches `READY`, `InspectionListener` looks up that restaurant's menu, visits only that subtree, then publishes an `INSPECTION_REPORT` event. The demo's report subscriber receives it.

**Concurrency:** `ProducerRunner` uses a fixed `ExecutorService` with configurable thread count. It waits for all futures, shuts down in `finally`, and awaits termination. Publication is synchronous, so a completed producer has completed its listener calls. No lock covers the whole subject. Tests check 4 threads × 1,000 events exactly.

**Composition root:** `Main` constructs and wires the sources, listeners, tree, and producer runner. Dependencies enter `InspectionListener` through its constructor; business classes do not lazily initialize collaborators.

## Trade-off proof

The Git history contains separate commits for adding `LeafNameVisitor` and adding `Side`. The new visitor changes `LeafNameVisitor.java`, `DeliveryPatternsTest.java`, and `docs/classes.md`; no element class changes. The new element changes `MenuNode.java`, `Visitor.java`, all five concrete visitor files, `DeliveryPatternsTest.java`, and `docs/classes.md`. Visitor is useful when new operations are common and element types are stable. If the menu gains new element types frequently, the required changes to every visitor make this design costly.

## Tests and diagram

`DeliveryPatternsTest` covers ordering, subscription changes from inside handlers, aggregation, the integration link, hand-calculated visitor values, JSON structure and escaping, and the concurrent exact-count stress case. UML class diagram: [docs/classes.md](docs/classes.md).

Repository: https://github.com/Natalyev/assignment_6. Submit this link with the required LMS report. The assignment asks for a two-week commit history; this project records the implementation steps honestly and does not invent earlier dates.
