package ohos.data.rdb;

import java.util.Iterator;
import java.util.Vector;

public class DataObservable {
    private final Object OBSERVER_LOCK = new Object();
    private Vector<DataObserver> observers = new Vector<>();

    public void add(DataObserver dataObserver) {
        if (dataObserver != null) {
            synchronized (this.OBSERVER_LOCK) {
                if (!this.observers.contains(dataObserver)) {
                    this.observers.add(dataObserver);
                }
            }
            return;
        }
        throw new IllegalArgumentException("input observer cannot be null!");
    }

    public void remove(DataObserver dataObserver) {
        if (dataObserver != null) {
            synchronized (this.OBSERVER_LOCK) {
                this.observers.remove(dataObserver);
            }
            return;
        }
        throw new IllegalArgumentException("input observer cannot be null!");
    }

    public void notifyObservers() {
        synchronized (this.OBSERVER_LOCK) {
            Iterator<DataObserver> it = this.observers.iterator();
            while (it.hasNext()) {
                it.next().onChange();
            }
        }
    }

    public void removeAll() {
        synchronized (this.OBSERVER_LOCK) {
            this.observers.clear();
        }
    }
}
