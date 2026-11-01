# Assignment 6 Report — Food Delivery

## Topic and objective

Variant 14, Food delivery. The project demonstrates Observer for live order and courier updates, Visitor for operations over a menu tree, and an Observer-to-Visitor integration link.

## Observer

`OrderSource` publishes order statuses and `CourierSource` publishes locations through generic `Subject<T>` and `Observer<T>` interfaces. Events are immutable records and subscriptions are keyed by `EventType`. Six listeners cover customer notifications, the restaurant panel, courier display, ETA tracking, aggregation, and inspection. `Aggregator` is also a subject and republishes count events. Push delivery was chosen because every event carries the values that listeners need.

The registry uses `ConcurrentHashMap` and `CopyOnWriteArrayList`. A listener can join or leave from its own handler. The list snapshot defines the current notification: membership changes affect later publications. `LongAdder` and concurrent maps protect listener state. `ProducerRunner` uses an `ExecutorService` with a configurable pool size, waits for each task, and terminates gracefully.

## Visitor

The tree has City → Restaurant → Menu → items. City, Restaurant, and Menu are containers. Dish, Combo, Drink, Dessert, and Side are leaf types. `Visitor<R>` supplies a visit overload for every concrete type; `accept` performs double dispatch. Traversal lives in each visitor, so an operation can decide which descendants to examine. `CaloriesVisitor` sums calories, `AllergenVisitor` accumulates allergens, `LowCalorieVisitor` returns matching leaves, `JsonVisitor` exports nested JSON, and `LeafNameVisitor` lists leaves.

For the demo menu, calories are 700 + 1100 + 0 + 350 = **2,150**. The integration listener visits only the menu belonging to the restaurant in the `READY` event, then publishes an `INSPECTION_REPORT` event containing the result.

## Change cost

Commit `402392b` adds `LeafNameVisitor` with no element changes. Commit `fe088e3` adds `Side` and changes `MenuNode.java`, `Visitor.java`, every concrete visitor, tests, diagram, and README. This favors Visitor when operations change more often than element types.

## Verification

The 21 JUnit tests passed locally using Java 17 compatibility. The concurrency stress test publishes 4 × 1,000 events and asserts an exact count of 4,000. The demo printed one inspection report and 4,000 published events. Maven was unavailable on the development machine, so the tests were compiled and executed with the locally cached JUnit 5 libraries. The repository includes a Maven build for standard submission environments.

## Submission

The GitHub repository URL must be added after publishing. The course asks for a meaningful two-week commit history; this repository records three genuine implementation stages and does not claim earlier work dates.
