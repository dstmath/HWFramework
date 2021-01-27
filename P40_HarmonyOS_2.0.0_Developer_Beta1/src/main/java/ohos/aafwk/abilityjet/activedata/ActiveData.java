package ohos.aafwk.abilityjet.activedata;

import java.util.HashMap;
import java.util.Map;
import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.ability.LifecycleStateObserver;
import ohos.aafwk.content.Intent;

public class ActiveData<T> {
    private static final int START_VERSION = -1;
    private int activeObserverCount = 0;
    private T data;
    private int dataVersion = -1;
    private final Object mapLock = new Object();
    private volatile boolean notifyDataInvalid = false;
    private boolean notifyingDataUpdated = false;
    private Map<DataObserver<T>, ActiveData<T>.DataLifecycleObserver> observers = new HashMap();

    static /* synthetic */ int access$112(ActiveData activeData, int i) {
        int i2 = activeData.activeObserverCount + i;
        activeData.activeObserverCount = i2;
        return i2;
    }

    public void addObserver(DataObserver<T> dataObserver, boolean z) {
        if (dataObserver != null) {
            ActiveData<T>.DataLifecycleObserver dataLifecycleObserver = new DataLifecycleObserver(dataObserver, z);
            synchronized (this.mapLock) {
                if (this.observers.putIfAbsent(dataObserver, dataLifecycleObserver) == null) {
                    dataObserver.setObserver(dataLifecycleObserver);
                    dataLifecycleObserver.onStateChanged(dataObserver.getLifecycleState(), null);
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("observer is illegal");
    }

    public void removeObserver(DataObserver<T> dataObserver) {
        synchronized (this.mapLock) {
            ActiveData<T>.DataLifecycleObserver remove = this.observers.remove(dataObserver);
            if (remove != null) {
                dataObserver.clearObserver(remove);
                remove.activeStateChanged(false);
            }
        }
    }

    public boolean hasObserver() {
        boolean z;
        synchronized (this.mapLock) {
            z = !this.observers.isEmpty();
        }
        return z;
    }

    public boolean hasActiveObserver() {
        return this.activeObserverCount != 0;
    }

    public void setData(T t) {
        this.data = t;
        this.dataVersion++;
        notifyDataUpdated(null);
    }

    public T getData() {
        return this.data;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyDataUpdated(ActiveData<T>.DataLifecycleObserver dataLifecycleObserver) {
        if (this.notifyingDataUpdated) {
            this.notifyDataInvalid = true;
            return;
        }
        this.notifyingDataUpdated = true;
        do {
            this.notifyDataInvalid = false;
            if (dataLifecycleObserver == null) {
                synchronized (this.mapLock) {
                    for (ActiveData<T>.DataLifecycleObserver dataLifecycleObserver2 : this.observers.values()) {
                        considerNotify(dataLifecycleObserver2);
                        if (this.notifyDataInvalid) {
                            break;
                        }
                    }
                }
            } else {
                considerNotify(dataLifecycleObserver);
                dataLifecycleObserver = null;
            }
        } while (this.notifyDataInvalid);
        this.notifyingDataUpdated = false;
    }

    private void considerNotify(ActiveData<T>.DataLifecycleObserver dataLifecycleObserver) {
        if (dataLifecycleObserver.shouldBeActive() && this.dataVersion != dataLifecycleObserver.lastVersion) {
            dataLifecycleObserver.lastVersion = this.dataVersion;
            dataLifecycleObserver.observer.onChanged(this.data);
        }
    }

    /* access modifiers changed from: package-private */
    public class DataLifecycleObserver implements LifecycleStateObserver {
        private boolean isActive = false;
        private final boolean isForever;
        int lastVersion = -1;
        final DataObserver<T> observer;

        DataLifecycleObserver(DataObserver<T> dataObserver, boolean z) {
            this.observer = dataObserver;
            this.isForever = z;
        }

        public boolean shouldBeActive() {
            Lifecycle.Event lifecycleState = this.observer.getLifecycleState();
            return this.isForever | (lifecycleState == Lifecycle.Event.ON_ACTIVE || lifecycleState == Lifecycle.Event.ON_INACTIVE);
        }

        @Override // ohos.aafwk.ability.LifecycleStateObserver
        public void onStateChanged(Lifecycle.Event event, Intent intent) {
            if (event == Lifecycle.Event.ON_STOP) {
                ActiveData.this.removeObserver(this.observer);
                return;
            }
            boolean z = event == Lifecycle.Event.ON_ACTIVE || event == Lifecycle.Event.ON_INACTIVE;
            if (z != this.isActive) {
                this.isActive = z;
                activeStateChanged(shouldBeActive());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void activeStateChanged(boolean z) {
            ActiveData.access$112(ActiveData.this, z ? 1 : -1);
            if (z) {
                ActiveData.this.notifyDataUpdated(this);
            }
        }
    }
}
