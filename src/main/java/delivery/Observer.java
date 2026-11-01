package delivery;

@FunctionalInterface
interface Observer<T> {
    void onEvent(Event<T> event);
}
