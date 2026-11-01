package delivery;

import java.util.Objects;

enum EventType { ORDER_STATUS, COURIER_LOCATION, AGGREGATE, INSPECTION_REPORT }

record Event<T>(EventType type, T payload) {
    Event { Objects.requireNonNull(type); Objects.requireNonNull(payload); }
}

record OrderStatus(String orderId, String restaurantId, String status) {
    OrderStatus { Objects.requireNonNull(orderId); Objects.requireNonNull(restaurantId); Objects.requireNonNull(status); }
}

record CourierLocation(String courierId, int minutesAway) {
    CourierLocation { Objects.requireNonNull(courierId); if (minutesAway < 0) throw new IllegalArgumentException("minutesAway"); }
}

record InspectionReport(String restaurantId, int calories, String json) { }
record AggregateCount(EventType observedType, long count) { }
