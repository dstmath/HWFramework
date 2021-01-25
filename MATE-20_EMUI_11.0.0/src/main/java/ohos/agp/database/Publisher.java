package ohos.agp.database;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Publisher<T> {
    protected final List<T> mSubscribers = new CopyOnWriteArrayList();

    public void registerSubscriber(T t) {
        if (t == null) {
            throw new IllegalArgumentException("The subscriber is null.");
        } else if (!this.mSubscribers.contains(t)) {
            this.mSubscribers.add(t);
        } else {
            throw new IllegalStateException("Publisher " + ((Object) t) + " is already registered.");
        }
    }

    public void unregisterSubscriber(T t) {
        if (t == null) {
            throw new IllegalArgumentException("The subscriber is null.");
        } else if (!this.mSubscribers.remove(t)) {
            throw new IllegalStateException("Subscriber " + ((Object) t) + " was not registered.");
        }
    }

    public void unregisterSubscriber(long j) {
        for (T t : this.mSubscribers) {
            if ((t instanceof DataSetSubscriberProxy) && t.mNativeDataSetObserver == j) {
                this.mSubscribers.remove(t);
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterAll() {
        this.mSubscribers.clear();
    }
}
