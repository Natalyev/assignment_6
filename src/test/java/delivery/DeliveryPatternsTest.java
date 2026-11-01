package delivery;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DeliveryPatternsTest {
    private Menu menu() { return new Menu("Lunch", List.of(new Dish("Pizza", 700, "milk"), new Combo("Box", 900, "wheat"), new Drink("Water", 0, ""), new Dessert("Cake", 300, "egg"))); }
    private City city() { return new City("City", List.of(new Restaurant("R1", List.of(menu())))); }

    @Test void caloriesMatchHandCalculation() { assertEquals(1900, city().accept(new CaloriesVisitor())); }
    @Test void newVisitorListsLeavesWithoutElementChanges() { assertEquals(List.of("Pizza", "Box", "Water", "Cake"), city().accept(new LeafNameVisitor())); }
    @Test void newElementWorksWithEveryVisitor() {
        Side side = new Side("Fries", 250, "wheat");
        assertEquals(250, side.accept(new CaloriesVisitor()));
        assertEquals(java.util.Set.of("wheat"), side.accept(new AllergenVisitor()));
        assertEquals(List.of(side), side.accept(new LowCalorieVisitor(250)));
        assertEquals(List.of("Fries"), side.accept(new LeafNameVisitor()));
        assertTrue(side.accept(new JsonVisitor()).contains("\"type\":\"side\""));
    }
    @Test void subtreeCaloriesExcludeOtherRestaurants() {
        City city = new City("City", List.of(new Restaurant("A", List.of(menu())), new Restaurant("B", List.of(new Menu("Small", List.of(new Drink("Tea", 5, "")))))));
        assertEquals(1900, city.children().get(0).accept(new CaloriesVisitor()));
        assertEquals(1905, city.accept(new CaloriesVisitor()));
    }
    @Test void allergenVisitorAccumulatesAcrossTree() { assertEquals(java.util.Set.of("milk", "wheat", "egg"), city().accept(new AllergenVisitor())); }
    @Test void allergenVisitorIgnoresEmptyValue() { assertEquals(java.util.Set.of(), new Drink("Water", 0, "").accept(new AllergenVisitor())); }
    @Test void filterReturnsOnlyMatchingLeaves() { assertEquals(List.of("Water"), city().accept(new LowCalorieVisitor(100)).stream().map(n -> ((Drink)n).name()).toList()); }
    @Test void filterIncludesBoundary() { assertEquals(1, new Drink("Tea", 100, "").accept(new LowCalorieVisitor(100)).size()); }
    @Test void jsonPreservesThreeLevelNesting() {
        String json = city().accept(new JsonVisitor());
        assertTrue(json.startsWith("{\"type\":\"city\",\"name\":\"City\",\"children\":[{\"type\":\"restaurant\""));
        assertTrue(json.contains("\"type\":\"menu\",\"name\":\"Lunch\",\"children\":["));
        assertTrue(json.endsWith("}]}]}"));
    }
    @Test void jsonEscapesQuotesAndControlCharacters() { assertTrue(new Dish("A\"\nB", 1, "").accept(new JsonVisitor()).contains("A\\\"\\u000aB")); }
    @Test void childrenAreImmutable() { assertThrows(UnsupportedOperationException.class, () -> menu().children().add(new Drink("X", 1, ""))); }
    @Test void subscribersReceiveOnlyTheirEventType() {
        EventSubject<Integer> subject = new EventSubject<>(); EventCounter<Integer> counter = new EventCounter<>();
        subject.subscribe(EventType.ORDER_STATUS, counter);
        subject.publish(new Event<>(EventType.COURIER_LOCATION, 1)); subject.publish(new Event<>(EventType.ORDER_STATUS, 2));
        assertEquals(1, counter.count());
    }
    @Test void duplicateSubscriptionDoesNotDuplicateDelivery() {
        EventSubject<Integer> subject = new EventSubject<>(); EventCounter<Integer> counter = new EventCounter<>();
        subject.subscribe(EventType.ORDER_STATUS, counter); subject.subscribe(EventType.ORDER_STATUS, counter);
        subject.publish(new Event<>(EventType.ORDER_STATUS, 1)); assertEquals(1, counter.count());
    }
    @Test void listenerCanUnsubscribeInsideHandler() {
        EventSubject<Integer> subject = new EventSubject<>(); AtomicInteger received = new AtomicInteger();
        @SuppressWarnings("unchecked") Observer<Integer>[] holder = new Observer[1];
        holder[0] = event -> { received.incrementAndGet(); subject.unsubscribe(EventType.ORDER_STATUS, holder[0]); };
        subject.subscribe(EventType.ORDER_STATUS, holder[0]);
        subject.publish(new Event<>(EventType.ORDER_STATUS, 1)); subject.publish(new Event<>(EventType.ORDER_STATUS, 2));
        assertEquals(1, received.get());
    }
    @Test void listenerCanSubscribeInsideHandler() {
        EventSubject<Integer> subject = new EventSubject<>(); EventCounter<Integer> late = new EventCounter<>();
        subject.subscribe(EventType.ORDER_STATUS, event -> subject.subscribe(EventType.ORDER_STATUS, late));
        subject.publish(new Event<>(EventType.ORDER_STATUS, 1)); subject.publish(new Event<>(EventType.ORDER_STATUS, 2));
        assertEquals(1, late.count());
    }
    @Test void aggregateListenerRepublishes() {
        EventSubject<Integer> source = new EventSubject<>(); Aggregator<Integer> aggregate = new Aggregator<>();
        AtomicInteger latest = new AtomicInteger();
        source.subscribe(EventType.ORDER_STATUS, aggregate);
        aggregate.subscribe(EventType.AGGREGATE, event -> latest.set((int) event.payload().count()));
        source.publish(new Event<>(EventType.ORDER_STATUS, 1)); source.publish(new Event<>(EventType.ORDER_STATUS, 2));
        assertEquals(2, latest.get());
    }
    @Test void orderEventInspectsOnlyRelatedMenu() {
        OrderSource source = new OrderSource(); EventSubject<InspectionReport> reports = new EventSubject<>();
        AtomicInteger calories = new AtomicInteger(); reports.subscribe(EventType.INSPECTION_REPORT, event -> calories.set(event.payload().calories()));
        source.subscribe(EventType.ORDER_STATUS, new InspectionListener(Map.of("R1", menu(), "R2", new Drink("Tea", 5, "")), reports));
        source.update(new OrderStatus("O1", "R1", "READY")); assertEquals(1900, calories.get());
    }
    @Test void nonReadyOrderDoesNotTriggerInspection() {
        OrderSource source = new OrderSource(); EventSubject<InspectionReport> reports = new EventSubject<>(); EventCounter<InspectionReport> counter = new EventCounter<>();
        reports.subscribe(EventType.INSPECTION_REPORT, counter);
        source.subscribe(EventType.ORDER_STATUS, new InspectionListener(Map.of("R1", menu()), reports));
        source.update(new OrderStatus("O1", "R1", "CREATED")); assertEquals(0, counter.count());
    }
    @Test void fourThreadsPublishExactlyFourThousandEvents() {
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            EventSubject<Integer> source = new EventSubject<>(); EventCounter<Integer> counter = new EventCounter<>();
            source.subscribe(EventType.ORDER_STATUS, counter);
            new ProducerRunner(4).run(4, 1000, source);
            assertEquals(4000, counter.count());
        });
    }
    @Test void producerCountMustBeAtLeastThree() { assertThrows(IllegalArgumentException.class, () -> new ProducerRunner(3).run(2, 1, new EventSubject<>())); }
    @Test void sourceModelsRejectInvalidData() { assertThrows(IllegalArgumentException.class, () -> new CourierLocation("C1", -1)); }
}
