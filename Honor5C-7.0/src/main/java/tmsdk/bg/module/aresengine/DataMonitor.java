package tmsdk.bg.module.aresengine;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
public abstract class DataMonitor<T extends TelephonyEntity> {
    private DataFilter<T> sr;
    private ConcurrentLinkedQueue<MonitorCallback<T>> wR;
    private Object wS;

    /* compiled from: Unknown */
    public static abstract class MonitorCallback<T extends TelephonyEntity> {
        private boolean wT;

        public MonitorCallback() {
            this.wT = false;
        }

        public void abortMonitor() {
            this.wT = true;
        }

        public abstract void onCallback(T t);
    }

    public DataMonitor() {
        this.wS = new Object();
        this.wR = new ConcurrentLinkedQueue();
    }

    protected void a(boolean z, T t, Object... objArr) {
    }

    public final void addCallback(MonitorCallback<T> monitorCallback) {
        this.wR.add(monitorCallback);
    }

    public void bind(DataFilter<T> dataFilter) {
        synchronized (this.wS) {
            this.sr = dataFilter;
        }
    }

    public final void notifyDataReached(T t, Object... objArr) {
        boolean z = false;
        if (t != null) {
            if (this.wR.size() > 0) {
                Iterator it = this.wR.iterator();
                while (it.hasNext()) {
                    MonitorCallback monitorCallback = (MonitorCallback) it.next();
                    monitorCallback.onCallback(t);
                    z = monitorCallback.wT;
                    if (z) {
                        break;
                    }
                }
            }
            a(z, t, objArr);
            if (!z) {
                synchronized (this.wS) {
                    if (this.sr != null) {
                        this.sr.filter(t, objArr);
                    }
                }
            }
        }
    }

    public final void removeCallback(MonitorCallback<T> monitorCallback) {
        this.wR.remove(monitorCallback);
    }

    public void setRegisterState(boolean z) {
    }

    public void unbind() {
        synchronized (this.wS) {
            this.sr = null;
        }
    }
}
