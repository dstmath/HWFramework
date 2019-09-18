package android.database;

import java.util.ArrayList;

public abstract class Observable<T> {
    protected final ArrayList<T> mObservers = new ArrayList<>();

    public void registerObserver(T observer) {
        if (observer != null) {
            synchronized (this.mObservers) {
                if (!this.mObservers.contains(observer)) {
                    this.mObservers.add(observer);
                } else {
                    throw new IllegalStateException("Observer " + observer + " is already registered.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("The observer is null.");
    }

    public void unregisterObserver(T observer) {
        if (observer != null) {
            synchronized (this.mObservers) {
                int index = this.mObservers.indexOf(observer);
                if (index != -1) {
                    this.mObservers.remove(index);
                } else {
                    throw new IllegalStateException("Observer " + observer + " was not registered.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("The observer is null.");
    }

    public void unregisterAll() {
        synchronized (this.mObservers) {
            this.mObservers.clear();
        }
    }
}
