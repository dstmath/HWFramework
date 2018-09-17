package java.util;

public abstract class EventListenerProxy<T extends EventListener> implements EventListener {
    private final T listener;

    public EventListenerProxy(T listener) {
        this.listener = listener;
    }

    public T getListener() {
        return this.listener;
    }
}
