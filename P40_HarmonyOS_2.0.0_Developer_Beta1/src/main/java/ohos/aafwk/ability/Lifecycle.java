package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.Iterator;
import ohos.aafwk.content.Intent;

public class Lifecycle {
    private Event curState = Event.UNDEFINED;
    private final Object observerLock = new Object();
    private ArrayList<ILifecycleObserver> observers = new ArrayList<>();

    public enum Event {
        UNDEFINED,
        ON_START,
        ON_INACTIVE,
        ON_ACTIVE,
        ON_BACKGROUND,
        ON_FOREGROUND,
        ON_STOP
    }

    public void dispatchLifecycle(Event event, Intent intent) {
        ArrayList arrayList;
        this.curState = event;
        synchronized (this.observerLock) {
            arrayList = new ArrayList(this.observers);
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof LifecycleStateObserver) {
                ((LifecycleStateObserver) next).onStateChanged(event, intent);
            } else if (next instanceof LifecycleObserver) {
                LifecycleObserver lifecycleObserver = (LifecycleObserver) next;
                switch (event) {
                    case ON_START:
                        lifecycleObserver.onStart(intent);
                        continue;
                    case ON_INACTIVE:
                        lifecycleObserver.onInactive();
                        continue;
                    case ON_ACTIVE:
                        lifecycleObserver.onActive();
                        continue;
                    case ON_BACKGROUND:
                        lifecycleObserver.onBackground();
                        continue;
                    case ON_FOREGROUND:
                        lifecycleObserver.onForeground(intent);
                        continue;
                    case ON_STOP:
                        lifecycleObserver.onStop();
                        continue;
                }
            }
        }
        if (this.curState == Event.ON_STOP) {
            this.curState = Event.UNDEFINED;
        }
    }

    public void addObserver(ILifecycleObserver iLifecycleObserver) {
        if (iLifecycleObserver != null) {
            synchronized (this.observerLock) {
                if (!this.observers.contains(iLifecycleObserver)) {
                    this.observers.add(iLifecycleObserver);
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("observer is illegal");
    }

    public void removeObserver(ILifecycleObserver iLifecycleObserver) {
        if (iLifecycleObserver != null) {
            synchronized (this.observerLock) {
                this.observers.remove(iLifecycleObserver);
            }
            return;
        }
        throw new IllegalArgumentException("observer is illegal");
    }

    public Event getLifecycleState() {
        return this.curState;
    }
}
