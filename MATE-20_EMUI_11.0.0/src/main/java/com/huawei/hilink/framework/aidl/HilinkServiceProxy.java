package com.huawei.hilink.framework.aidl;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifi.wifinearfind.HwWifiNearFindUtils;
import com.huawei.hilink.framework.aidl.IHilinkService;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HilinkServiceProxy implements Closeable {
    private static final long DESTORY_WAITING_TIME = 1000;
    public static final int ERRORCODE_BAD_REQUEST = 400;
    public static final int ERRORCODE_INITIALIZATION_FAILURE = 12;
    public static final int ERRORCODE_MAX_SERVICE_NUM_REACHED = 10;
    public static final int ERRORCODE_METHOD_NOT_ALLOWED = 405;
    public static final int ERRORCODE_NOT_FOUND = 404;
    public static final int ERRORCODE_NO_NETWORK = 3;
    public static final int ERRORCODE_NULLPOINTER = 2;
    public static final int ERRORCODE_OK = 0;
    public static final int ERRORCODE_PERMISSION_DENIED = 11;
    public static final int ERRORCODE_REQUEST_ILLEGAL = 5;
    public static final int ERRORCODE_REQUEST_NOLONGER_EXIST = 8;
    public static final int ERRORCODE_RUNTIME = 4;
    public static final int ERRORCODE_SERVICE_ALREADY_EXIST = 7;
    public static final int ERRORCODE_TASK_QUEUE_FULL = 6;
    private static final String TAG = "hilinkService";
    private static long destroyTime = 0;
    private HilinkServiceConnection conn;
    private final ReentrantLock connLock = new ReentrantLock();
    private Context context;
    private IHilinkService hilinkServiceBinder;
    private volatile boolean isBinded = false;
    private boolean isServiceConnected = false;
    private final Condition serviceConnected = this.connLock.newCondition();

    public HilinkServiceProxy(Context context2) {
        bindService(context2, null);
    }

    public HilinkServiceProxy(Context context2, HilinkServiceProxyState state) {
        bindService(context2, state);
        if (state == null) {
            Log.e(TAG, "null state");
        }
    }

    private void bindService(Context context2, HilinkServiceProxyState state) {
        if (context2 != null) {
            this.context = context2;
            long destroyedTime = SystemClock.elapsedRealtime() - destroyTime;
            Log.d(TAG, "destroyedTime: " + destroyedTime);
            if (destroyedTime >= 0 && destroyedTime < DESTORY_WAITING_TIME) {
                SystemClock.sleep(DESTORY_WAITING_TIME - destroyedTime);
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(HwWifiNearFindUtils.HILINK_APK, "com.huawei.hilink.framework.HilinkService"));
            this.conn = new HilinkServiceConnection(state);
            this.isBinded = context2.bindService(intent, this.conn, 1);
            return;
        }
        Log.e(TAG, "null context");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void signalServiceConnected() {
        ReentrantLock takeLock = this.connLock;
        takeLock.lock();
        try {
            this.serviceConnected.signal();
        } finally {
            takeLock.unlock();
        }
    }

    private IHilinkService getHilinkServiceBinder() {
        if (this.isServiceConnected) {
            return this.hilinkServiceBinder;
        }
        ReentrantLock lock = this.connLock;
        try {
            lock.lockInterruptibly();
            long nanos = TimeUnit.SECONDS.toNanos(3);
            while (!this.isServiceConnected && this.hilinkServiceBinder == null) {
                if (nanos <= 0) {
                    IHilinkService iHilinkService = this.hilinkServiceBinder;
                    lock.unlock();
                    return iHilinkService;
                }
                nanos = this.serviceConnected.awaitNanos(nanos);
            }
        } catch (InterruptedException e) {
        } catch (Throwable th) {
            lock.unlock();
            throw th;
        }
        lock.unlock();
        return this.hilinkServiceBinder;
    }

    public int discover(DiscoverRequest request, ServiceFoundCallbackWrapper callback) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 == null) {
            return 12;
        }
        try {
            return hilinkServiceBinder2.discover(request, callback);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int call(CallRequest request, ResponseCallbackWrapper callback) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 == null) {
            return 12;
        }
        try {
            return hilinkServiceBinder2.call(request, callback);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int connect(ConnectRequest request, ConnectResultCallbackWrapper callback) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 == null) {
            return 12;
        }
        try {
            return hilinkServiceBinder2.connect(request, callback);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int publish(String serviceType, String serviceID, RequestHandlerWrapper requestHandler) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 == null) {
            return 12;
        }
        try {
            return hilinkServiceBinder2.publishKeepOnline(serviceType, serviceID, requestHandler);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int publish(String serviceType, String serviceID, PendingIntent pendingIntent) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 == null) {
            return 12;
        }
        try {
            return hilinkServiceBinder2.publishCanbeOffline(serviceType, serviceID, pendingIntent);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public static CallRequest getCallRequest(Intent intent) {
        Bundle bundle;
        if (intent == null || (bundle = intent.getExtras()) == null) {
            return null;
        }
        return (CallRequest) bundle.get("CallRequest");
    }

    public void unpublish(String serviceID) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 != null) {
            try {
                hilinkServiceBinder2.unpublish(serviceID);
            } catch (RemoteException e) {
                Log.e(TAG, "unpublish failed");
            }
        }
    }

    public int sendResponse(int errorCode, String payload, CallRequest callRequest) {
        IHilinkService hilinkServiceBinder2 = getHilinkServiceBinder();
        if (hilinkServiceBinder2 == null) {
            return 12;
        }
        try {
            return hilinkServiceBinder2.sendResponse(errorCode, payload, callRequest);
        } catch (RemoteException e) {
            return 4;
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        if (this.context != null && this.isBinded) {
            this.isBinded = false;
            this.conn.close();
            this.context.unbindService(this.conn);
            destroyTime = SystemClock.elapsedRealtime();
        }
    }

    /* access modifiers changed from: private */
    public class HilinkServiceConnection implements ServiceConnection {
        private IConnectionStateCallback callback;
        private HilinkServiceProxyState proxyState;

        private HilinkServiceConnection(HilinkServiceProxyState state) {
            this.proxyState = state;
            HilinkServiceProxyState hilinkServiceProxyState = this.proxyState;
            if (hilinkServiceProxyState != null) {
                this.callback = new ConnectionStateCallbackWrapper(hilinkServiceProxyState);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(HilinkServiceProxy.TAG, "onServiceConnected");
            HilinkServiceProxy.this.hilinkServiceBinder = IHilinkService.Stub.asInterface(service);
            if (HilinkServiceProxy.this.hilinkServiceBinder != null) {
                try {
                    HilinkServiceProxy.this.hilinkServiceBinder.registerConnectionStateCallback(this.callback);
                } catch (RemoteException e) {
                    Log.e(HilinkServiceProxy.TAG, "RemoteException: register callback failed");
                }
            }
            HilinkServiceProxy.this.isServiceConnected = true;
            HilinkServiceProxy.this.signalServiceConnected();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.i(HilinkServiceProxy.TAG, "onServiceDisconnected");
            close();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void close() {
            if (!(HilinkServiceProxy.this.hilinkServiceBinder == null || this.callback == null)) {
                try {
                    HilinkServiceProxy.this.hilinkServiceBinder.unregisterConnectionStateCallback(this.callback);
                } catch (RemoteException e) {
                    Log.e(HilinkServiceProxy.TAG, "RemoteException: unregister callback failed");
                }
                this.callback = null;
            }
            HilinkServiceProxy.this.hilinkServiceBinder = null;
            HilinkServiceProxyState hilinkServiceProxyState = this.proxyState;
            if (hilinkServiceProxyState != null) {
                hilinkServiceProxyState.onProxyLost();
                this.proxyState = null;
            }
        }
    }
}
