package java.util;

import java.util.EventListener;

public abstract class EventListenerProxy<T extends EventListener> implements EventListener {
    private final T listener;

    public EventListenerProxy(T listener2) {
        this.listener = listener2;
    }

    public T getListener() {
        return this.listener;
    }
}
