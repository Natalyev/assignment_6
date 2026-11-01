package delivery;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class EventSubject<T> implements Subject<T> {
    private final ConcurrentHashMap<EventType, CopyOnWriteArrayList<Observer<T>>> listeners = new ConcurrentHashMap<>();

    public void subscribe(EventType type, Observer<T> observer) {
        listeners.computeIfAbsent(Objects.requireNonNull(type), ignored -> new CopyOnWriteArrayList<>())
                .addIfAbsent(Objects.requireNonNull(observer));
    }

    public void unsubscribe(EventType type, Observer<T> observer) {
        var group = listeners.get(Objects.requireNonNull(type));
        if (group != null) group.remove(Objects.requireNonNull(observer));
    }

    public void publish(Event<T> event) {
        Objects.requireNonNull(event);
        var group = listeners.get(event.type());
        if (group != null) for (var observer : group) observer.onEvent(event);
    }
}
