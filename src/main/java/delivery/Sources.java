package delivery;

final class OrderSource extends EventSubject<OrderStatus> {
    void update(OrderStatus status) { publish(new Event<>(EventType.ORDER_STATUS, status)); }
}

final class CourierSource extends EventSubject<CourierLocation> {
    void update(CourierLocation location) { publish(new Event<>(EventType.COURIER_LOCATION, location)); }
}
