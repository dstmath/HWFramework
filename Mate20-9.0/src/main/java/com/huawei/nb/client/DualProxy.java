package com.huawei.nb.client;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import com.huawei.nb.client.Proxy;
import com.huawei.nb.utils.Waiter;
import com.huawei.nb.utils.logger.DSLog;

public abstract class DualProxy<P extends Proxy, T> extends Proxy<T> {
    private static final long CONNECT_TIMEOUT = 3000;
    protected final P secondary;

    public DualProxy(Context context, String serviceName, String action, @NonNull P secondary2) {
        super(context, serviceName, action);
        this.secondary = secondary2;
    }

    public boolean connect() {
        if (hasConnected()) {
            return true;
        }
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            DSLog.e("Failed to connect to %s, error: connect method can't be invoked in the main thread", this.serviceName);
            return false;
        }
        final Waiter waiter = new Waiter();
        if (!this.secondary.connect(new ServiceConnectCallback() {
            public void onConnect() {
                boolean unused = DualProxy.super.connect(new Proxy.ConnectCallback(waiter));
            }

            public void onDisconnect() {
            }
        })) {
            return false;
        }
        if (waiter.await(CONNECT_TIMEOUT)) {
            return true;
        }
        DSLog.e("Failed to connect to %s in %s ms.", this.serviceName, Long.valueOf(CONNECT_TIMEOUT));
        return false;
    }

    public boolean connect(final ServiceConnectCallback callback) {
        return this.secondary.connect(new ServiceConnectCallback() {
            public void onConnect() {
                boolean unused = DualProxy.super.connect(callback);
            }

            public void onDisconnect() {
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean disconnectInner() {
        return this.secondary.disconnectInner() && super.disconnectInner();
    }
}
