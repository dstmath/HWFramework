package com.huawei.nb.notification;

import com.huawei.nb.utils.logger.DSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LocalObservable<INFO, OBSERVER, REMOTE> {
    private static final Object PRESENT = new Object();
    private final Object lock = new Object();
    private final Map<INFO, Map<OBSERVER, Object>> modelObserverMap = new HashMap();
    private volatile REMOTE remoteService;

    /* access modifiers changed from: protected */
    public abstract boolean registerModelRemoteObserver(INFO info);

    /* access modifiers changed from: protected */
    public abstract boolean unregisterModelRemoteObserver(INFO info);

    public boolean registerObserver(INFO info, OBSERVER observer) {
        boolean z = false;
        if (info == null || observer == null) {
            DSLog.e("Failed to register observer, error: null observer information.", new Object[0]);
        } else {
            synchronized (this.lock) {
                Map<OBSERVER, Object> observers = this.modelObserverMap.get(info);
                if (observers != null) {
                    if (observers.containsKey(observer)) {
                        DSLog.w("The same observer for has been registered already.", new Object[0]);
                    } else {
                        observers.put(observer, PRESENT);
                    }
                } else if (!registerModelRemoteObserver(info)) {
                    DSLog.e("Failed to register remote observer.", new Object[0]);
                } else {
                    Map<OBSERVER, Object> observers2 = new HashMap<>();
                    observers2.put(observer, PRESENT);
                    this.modelObserverMap.put(info, observers2);
                }
                z = true;
            }
        }
        return z;
    }

    public boolean unregisterObserver(INFO info, OBSERVER observer) {
        boolean result = false;
        if (info == null || observer == null) {
            DSLog.e("Failed to unregister observer, error: null observer information to unregister", new Object[0]);
        } else {
            synchronized (this.lock) {
                Map<OBSERVER, Object> observers = this.modelObserverMap.get(info);
                if (observers != null) {
                    if (observers.remove(observer) != null) {
                        result = true;
                    }
                    if (observers.isEmpty()) {
                        unregisterModelRemoteObserver(info);
                        this.modelObserverMap.remove(info);
                    }
                }
            }
        }
        return result;
    }

    public void setRemoteService(REMOTE remoteService2) {
        this.remoteService = remoteService2;
        if (remoteService2 != null) {
            registerAll();
        }
    }

    public void unsetRemoteService() {
        if (this.remoteService != null) {
            unregisterAll();
        }
        this.remoteService = null;
    }

    /* access modifiers changed from: protected */
    public REMOTE getRemoteService() {
        return this.remoteService;
    }

    /* access modifiers changed from: protected */
    public List<OBSERVER> getObservers(INFO info) {
        ArrayList arrayList;
        synchronized (this.lock) {
            Map<OBSERVER, Object> set = this.modelObserverMap.get(info);
            arrayList = (set == null || set.isEmpty()) ? null : new ArrayList(set.keySet());
        }
        return arrayList;
    }

    private void unregisterAll() {
        synchronized (this.lock) {
            for (Map.Entry<INFO, Map<OBSERVER, Object>> entry : this.modelObserverMap.entrySet()) {
                unregisterModelRemoteObserver(entry.getKey());
                entry.getValue().clear();
            }
            this.modelObserverMap.clear();
        }
    }

    private void registerAll() {
        synchronized (this.lock) {
            for (INFO info : this.modelObserverMap.keySet()) {
                registerModelRemoteObserver(info);
            }
        }
    }
}
