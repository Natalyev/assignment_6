package delivery;

import java.util.List;
import java.util.Map;

public final class Main {
    public static void main(String[] args) throws InterruptedException {
        int threads = args.length == 0 ? 4 : Integer.parseInt(args[0]);
        Menu menu = new Menu("Lunch", List.of(new Dish("Pizza", 700, "milk"),
                new Combo("Family combo", 1100, "wheat"), new Drink("Water", 0, ""),
                new Dessert("Cake", 350, "egg")));
        City city = new City("Qyzylorda", List.of(new Restaurant("R1", List.of(menu))));
        OrderSource orders = new OrderSource();
        CourierSource couriers = new CourierSource();
        EventSubject<InspectionReport> reports = new EventSubject<>();
        CustomerApp customer = new CustomerApp();
        RestaurantPanel panel = new RestaurantPanel();
        CourierApp courierApp = new CourierApp();
        EtaTracker eta = new EtaTracker();
        Aggregator<OrderStatus> aggregate = new Aggregator<>();
        EventCounter<AggregateCount> aggregateSink = new EventCounter<>();
        EventCounter<InspectionReport> reportSink = new EventCounter<>();
        orders.subscribe(EventType.ORDER_STATUS, customer);
        orders.subscribe(EventType.ORDER_STATUS, panel);
        orders.subscribe(EventType.ORDER_STATUS, aggregate);
        orders.subscribe(EventType.ORDER_STATUS, new InspectionListener(Map.of("R1", menu), reports));
        couriers.subscribe(EventType.COURIER_LOCATION, courierApp);
        couriers.subscribe(EventType.COURIER_LOCATION, eta);
        aggregate.subscribe(EventType.AGGREGATE, aggregateSink);
        reports.subscribe(EventType.INSPECTION_REPORT, reportSink);
        orders.update(new OrderStatus("O1", "R1", "READY"));
        couriers.update(new CourierLocation("C1", 12));
        EventSubject<Integer> stressSource = new EventSubject<>();
        EventCounter<Integer> stressCounter = new EventCounter<>();
        stressSource.subscribe(EventType.ORDER_STATUS, stressCounter);
        new ProducerRunner(threads).run(4, 1000, stressSource);
        System.out.println("City calories: " + city.accept(new CaloriesVisitor()));
        System.out.println("Allergens: " + city.accept(new AllergenVisitor()));
        System.out.println("Inspection reports: " + reportSink.count());
        System.out.println("Published from four producers: " + stressCounter.count());
    }
}
