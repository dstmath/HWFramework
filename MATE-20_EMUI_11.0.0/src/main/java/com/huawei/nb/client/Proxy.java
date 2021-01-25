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
    private static final long CONNECT_TIMEOUT = 10000;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    protected final CallbackManager callbackManager;
    protected volatile long callbackTimeout;
    private volatile ServiceConnectCallback connectCallback;
    protected final Context context;
    private final RemoteServiceConnection dsConnection;
    private final int id;
    protected final LocalObservable<?, ?, T> localObservable;
    private final Object mLock;
    protected final String pkgName;
    protected volatile T remote;
    protected final String serviceName;

    /* access modifiers changed from: protected */
    public abstract T asInterface(IBinder iBinder);

    /* access modifiers changed from: protected */
    public abstract LocalObservable<?, ?, T> newLocalObservable();

    protected static class ConnectCallback implements ServiceConnectCallback {
        private final Waiter waiter;

        public ConnectCallback(Waiter waiter2) {
            this.waiter = waiter2;
        }

        @Override // com.huawei.nb.client.ServiceConnectCallback
        public void onConnect() {
            Waiter waiter2 = this.waiter;
            if (waiter2 != null) {
                waiter2.signal();
            }
        }

        @Override // com.huawei.nb.client.ServiceConnectCallback
        public void onDisconnect() {
            Waiter waiter2 = this.waiter;
            if (waiter2 != null) {
                waiter2.signal();
            }
        }
    }

    protected Proxy(Context context2, String str, String str2) {
        this(context2, ID_GENERATOR.incrementAndGet(), str, new RemoteServiceConnection(context2, str2));
    }

    protected Proxy(Context context2, String str, String str2, String str3) {
        this(context2, ID_GENERATOR.incrementAndGet(), str, new RemoteServiceConnection(context2, str2, str3));
    }

    protected Proxy(Context context2, int i, String str, String str2, String str3) {
        this(context2, i, str, new RemoteServiceConnection(context2, str2, str3));
    }

    private Proxy(Context context2, int i, String str, RemoteServiceConnection remoteServiceConnection) {
        String str2 = null;
        this.remote = null;
        this.callbackTimeout = 0;
        this.mLock = new Object();
        this.connectCallback = null;
        this.id = i;
        this.context = context2;
        this.serviceName = str;
        this.dsConnection = remoteServiceConnection;
        this.pkgName = context2 != null ? context2.getPackageName() : str2;
        this.callbackManager = new CallbackManager();
        this.localObservable = newLocalObservable();
        DSLog.init("HwNaturalBaseClient");
    }

    public boolean connect() {
        if (hasConnected()) {
            return true;
        }
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            DSLog.e("Failed to connect to %s, error: connect is invoked in the main thread", this.serviceName);
            return false;
        }
        Waiter waiter = new Waiter();
        if (connect(new ConnectCallback(waiter))) {
            if (waiter.await(CONNECT_TIMEOUT)) {
                return true;
            }
            DSLog.e("Failed to connect to %s in %s ms.", this.serviceName, Long.valueOf((long) CONNECT_TIMEOUT));
        }
        return false;
    }

    public boolean connect(ServiceConnectCallback serviceConnectCallback) {
        synchronized (this.mLock) {
            if (this.remote != null) {
                if (serviceConnectCallback != null) {
                    invokeCallbackInThread(serviceConnectCallback);
                }
                return true;
            }
            this.connectCallback = serviceConnectCallback;
            if (this.dsConnection.open(new ConnectionListener())) {
                DSLog.d("Succeed to open connection to %s.", this.serviceName);
                return true;
            }
            DSLog.e("Failed to open connection to %s.", this.serviceName);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean disconnectInner() {
        synchronized (this.mLock) {
            if (this.remote != null) {
                onDisconnect(true);
                DSLog.w("Connection to %s is closed completely.", this.serviceName);
                this.dsConnection.close();
            } else {
                if (this.localObservable != null) {
                    this.localObservable.stop();
                }
                DSLog.i("Connection to %s has been closed already.", this.serviceName);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean virtualDisconnectInner() {
        synchronized (this.mLock) {
            if (this.remote != null) {
                onDisconnect(false);
                DSLog.w("Connection to %s is closed virtually.", this.serviceName);
                this.dsConnection.close();
            } else {
                DSLog.i("Connection to %s has been closed already.", this.serviceName);
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001e: APUT  
      (r0v0 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Boolean : 0x001a: INVOKE  (r1v2 java.lang.Boolean) = (r1v1 boolean) type: STATIC call: java.lang.Boolean.valueOf(boolean):java.lang.Boolean)
     */
    /* access modifiers changed from: public */
    private void onConnect(IBinder iBinder) {
        this.remote = asInterface(iBinder);
        LocalObservable<?, ?, T> localObservable2 = this.localObservable;
        if (localObservable2 != null) {
            localObservable2.start(this.remote);
        }
        Object[] objArr = new Object[1];
        objArr[0] = Boolean.valueOf(this.remote != null);
        DSLog.i("Connection got remote is %s", objArr);
        invokeConnectCallback(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDisconnect(boolean z) {
        this.remote = null;
        invokeConnectCallback(false);
        this.callbackManager.interruptAll();
        LocalObservable<?, ?, T> localObservable2 = this.localObservable;
        if (localObservable2 == null) {
            return;
        }
        if (z) {
            localObservable2.stop();
        } else {
            localObservable2.pause();
        }
    }

    public boolean hasConnected() {
        return this.remote != null;
    }

    public int getId() {
        return this.id;
    }

    public void setExecutionTimeout(long j) {
        if (j > 0) {
            this.callbackTimeout = j;
        }
    }

    private void invokeConnectCallback(boolean z) {
        if (this.connectCallback == null) {
            DSLog.i("Not process callback: connectCallback is null", new Object[0]);
        } else if (z) {
            invokeCallbackInThread(this.connectCallback);
        } else {
            this.connectCallback.onDisconnect();
        }
    }

    /* access modifiers changed from: private */
    public class ConnectionListener implements RemoteServiceConnection.OnConnectListener {
        private ConnectionListener() {
        }

        @Override // com.huawei.nb.client.RemoteServiceConnection.OnConnectListener
        public void onConnect(IBinder iBinder) {
            if (iBinder != null) {
                Proxy.this.onConnect(iBinder);
                DSLog.i("Succeed to connect to %s.", Proxy.this.serviceName);
                return;
            }
            DSLog.i("Not process callback: binder is null.", new Object[0]);
        }

        @Override // com.huawei.nb.client.RemoteServiceConnection.OnConnectListener
        public void onDisconnect() {
            Proxy.this.onDisconnect(false);
            DSLog.w("Connection to %s is broken down.", Proxy.this.serviceName);
        }
    }

    private void invokeCallbackInThread(final ServiceConnectCallback serviceConnectCallback) {
        new Thread("connectCallback") {
            /* class com.huawei.nb.client.Proxy.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                serviceConnectCallback.onConnect();
            }
        }.start();
    }
}
