package com.huawei.nb.client;

import android.content.Context;
import android.os.IBinder;
import android.os.Looper;
import com.huawei.nb.client.RemoteServiceConnection;
import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.notification.LocalObservable;
import com.huawei.nb.utils.Waiter;
import com.huawei.nb.utils.logger.DSLog;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Proxy<T> {
    private static final long CONNECT_TIMEOUT = 3000;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    protected final CallbackManager callbackManager;
    protected volatile long callbackTimeout;
    private volatile ServiceConnectCallback connectCallback;
    protected final Context context;
    private final RemoteServiceConnection dsConnection;
    /* access modifiers changed from: private */
    public volatile boolean hasBinded;
    /* access modifiers changed from: private */
    public volatile boolean hasConnected;
    private final int id;
    protected final LocalObservable<?, ?, T> localObservable;
    private final Object mLock;
    protected final String pkgName;
    protected volatile T remote;
    protected final String serviceName;

    protected static class ConnectCallback implements ServiceConnectCallback {
        private final Waiter waiter;

        public ConnectCallback(Waiter waiter2) {
            this.waiter = waiter2;
        }

        public void onConnect() {
            if (this.waiter != null) {
                this.waiter.signal();
            }
        }

        public void onDisconnect() {
            if (this.waiter != null) {
                this.waiter.signal();
            }
        }
    }

    private class ConnectionListener implements RemoteServiceConnection.OnConnectListener {
        private ConnectionListener() {
        }

        public void onConnect(IBinder binder) {
            if (binder != null) {
                Proxy.this.remote = Proxy.this.asInterface(binder);
                boolean unused = Proxy.this.hasConnected = true;
                Proxy.this.onConnect();
                Proxy.this.invokeConnectCallback(true);
                DSLog.i("Succeed to connect to %s.", Proxy.this.serviceName);
            }
        }

        public void onDisconnect() {
            Proxy.this.remote = null;
            boolean unused = Proxy.this.hasConnected = false;
            boolean unused2 = Proxy.this.hasBinded = false;
            Proxy.this.invokeConnectCallback(false);
            Proxy.this.callbackManager.interruptAll();
            Proxy.this.onDisconnect(true);
            DSLog.w("Connection to %s is broken down.", Proxy.this.serviceName);
        }
    }

    /* access modifiers changed from: protected */
    public abstract T asInterface(IBinder iBinder);

    /* access modifiers changed from: protected */
    public abstract LocalObservable<?, ?, T> newLocalObservable();

    protected Proxy(Context context2, String serviceName2, String action) {
        this(context2, serviceName2, new RemoteServiceConnection(context2, action));
    }

    protected Proxy(Context context2, String serviceName2, String servicePackageName, String serviceClassName) {
        this(context2, serviceName2, new RemoteServiceConnection(context2, servicePackageName, serviceClassName));
    }

    private Proxy(Context context2, String serviceName2, RemoteServiceConnection connection) {
        this.mLock = new Object();
        this.remote = null;
        this.callbackTimeout = 0;
        this.connectCallback = null;
        this.hasConnected = false;
        this.hasBinded = false;
        this.id = ID_GENERATOR.incrementAndGet();
        this.context = context2;
        this.serviceName = serviceName2;
        this.dsConnection = connection;
        this.pkgName = context2.getPackageName();
        this.callbackManager = new CallbackManager();
        this.localObservable = newLocalObservable();
        DSLog.init("HwNaturalBaseClient");
    }

    public boolean connect() {
        if (hasConnected()) {
            return true;
        }
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            DSLog.e("Failed to connect to %s, error: connect method can't be invoked in the main thread", this.serviceName);
            return false;
        }
        Waiter waiter = new Waiter();
        if (!connect(new ConnectCallback(waiter))) {
            return false;
        }
        if (waiter.await(CONNECT_TIMEOUT)) {
            return true;
        }
        DSLog.e("Failed to connect to %s in %s ms.", this.serviceName, Long.valueOf(CONNECT_TIMEOUT));
        return false;
    }

    public boolean connect(ServiceConnectCallback callback) {
        boolean z = true;
        synchronized (this.mLock) {
            if (!this.hasBinded) {
                this.connectCallback = callback;
                if (this.remote != null) {
                    invokeConnectCallback(true);
                }
                this.hasBinded = this.dsConnection.open(new ConnectionListener());
                if (!this.hasBinded) {
                    DSLog.e("Failed to open connection to %s.", this.serviceName);
                }
                z = this.hasBinded;
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean disconnectInner() {
        synchronized (this.mLock) {
            if (this.hasBinded) {
                this.remote = null;
                this.hasConnected = false;
                this.hasBinded = false;
                invokeConnectCallback(false);
                this.callbackManager.interruptAll();
                onDisconnect(false);
                DSLog.w("Connection to %s is closed.", this.serviceName);
                this.dsConnection.close();
            } else {
                DSLog.i("Connection to %s has been closed already.", this.serviceName);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onConnect() {
        if (this.localObservable != null) {
            this.localObservable.setRemoteService(this.remote);
        }
    }

    /* access modifiers changed from: protected */
    public void onDisconnect(boolean isBroken) {
        if (!isBroken && this.localObservable != null) {
            this.localObservable.unsetRemoteService();
        }
    }

    public boolean hasConnected() {
        return this.hasConnected;
    }

    public int getId() {
        return this.id;
    }

    public void setExecutionTimeout(long timeout) {
        if (timeout > 0) {
            this.callbackTimeout = timeout;
        }
    }

    /* access modifiers changed from: private */
    public void invokeConnectCallback(boolean connected) {
        if (this.connectCallback == null) {
            return;
        }
        if (connected) {
            this.connectCallback.onConnect();
        } else {
            this.connectCallback.onDisconnect();
        }
    }
}
