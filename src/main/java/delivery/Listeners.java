package delivery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

final class EventCounter<T> implements Observer<T> {
    private final LongAdder count = new LongAdder();
    public void onEvent(Event<T> event) { count.increment(); }
    long count() { return count.sum(); }
}

final class CustomerApp implements Observer<OrderStatus> {
    private final Map<String, String> statuses = new ConcurrentHashMap<>();
    public void onEvent(Event<OrderStatus> event) { statuses.put(event.payload().orderId(), event.payload().status()); }
    String status(String orderId) { return statuses.get(orderId); }
}

final class RestaurantPanel implements Observer<OrderStatus> {
    private final LongAdder updates = new LongAdder();
    public void onEvent(Event<OrderStatus> event) { updates.increment(); }
    long updates() { return updates.sum(); }
}

final class CourierApp implements Observer<CourierLocation> {
    private final Map<String, Integer> locations = new ConcurrentHashMap<>();
    public void onEvent(Event<CourierLocation> event) { locations.put(event.payload().courierId(), event.payload().minutesAway()); }
    Integer minutesAway(String courierId) { return locations.get(courierId); }
}

final class EtaTracker implements Observer<CourierLocation> {
    private final LongAdder updates = new LongAdder();
    public void onEvent(Event<CourierLocation> event) { updates.increment(); }
    long updates() { return updates.sum(); }
}

final class Aggregator<T> extends EventSubject<AggregateCount> implements Observer<T> {
    private final LongAdder count = new LongAdder();
    public void onEvent(Event<T> event) {
        count.increment();
        publish(new Event<>(EventType.AGGREGATE, new AggregateCount(event.type(), count.sum())));
    }
}

final class InspectionListener implements Observer<OrderStatus> {
    private final Map<String, MenuNode> menus;
    private final Subject<InspectionReport> reports;
    InspectionListener(Map<String, MenuNode> menus, Subject<InspectionReport> reports) {
        this.menus = Map.copyOf(menus);
        this.reports = reports;
    }
    public void onEvent(Event<OrderStatus> event) {
        OrderStatus status = event.payload();
        if (!status.status().equals("READY")) return;
        MenuNode relatedMenu = menus.get(status.restaurantId());
        if (relatedMenu == null) return;
        reports.publish(new Event<>(EventType.INSPECTION_REPORT,
                new InspectionReport(status.restaurantId(), relatedMenu.accept(new CaloriesVisitor()), relatedMenu.accept(new JsonVisitor()))));
    }
}
