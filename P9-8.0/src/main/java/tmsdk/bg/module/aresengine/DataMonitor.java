package tmsdk.bg.module.aresengine;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.module.aresengine.TelephonyEntity;

public abstract class DataMonitor<T extends TelephonyEntity> {
    private DataFilter<T> pS;
    private ConcurrentLinkedQueue<MonitorCallback<T>> tV = new ConcurrentLinkedQueue();
    private Object tW = new Object();

    public static abstract class MonitorCallback<T extends TelephonyEntity> {
        private boolean tX = false;

        public void abortMonitor() {
            this.tX = true;
        }

        public abstract void onCallback(T t);
    }

    protected void a(boolean z, T t, Object... objArr) {
    }

    public final void addCallback(MonitorCallback<T> monitorCallback) {
        this.tV.add(monitorCallback);
    }

    public void bind(DataFilter<T> dataFilter) {
        synchronized (this.tW) {
            this.pS = dataFilter;
        }
    }

    public final void notifyDataReached(T t, Object... objArr) {
        if (t != null) {
            boolean z = false;
            if (this.tV.size() > 0) {
                Iterator it = this.tV.iterator();
                while (it.hasNext()) {
                    MonitorCallback monitorCallback = (MonitorCallback) it.next();
                    monitorCallback.onCallback(t);
                    z = monitorCallback.tX;
                    if (z) {
                        break;
                    }
                }
            }
            a(z, t, objArr);
            if (!z) {
                synchronized (this.tW) {
                    if (this.pS != null) {
                        this.pS.filter(t, objArr);
                    }
                }
            }
        }
    }

    public final void removeCallback(MonitorCallback<T> monitorCallback) {
        this.tV.remove(monitorCallback);
    }

    public void setRegisterState(boolean z) {
    }

    public void unbind() {
        synchronized (this.tW) {
            this.pS = null;
        }
    }
}
