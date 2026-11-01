package delivery;

interface Subject<T> {
    void subscribe(EventType type, Observer<T> observer);
    void unsubscribe(EventType type, Observer<T> observer);
    void publish(Event<T> event);
}
