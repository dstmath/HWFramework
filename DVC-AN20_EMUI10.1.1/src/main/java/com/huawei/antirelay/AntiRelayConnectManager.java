package com.huawei.antirelay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.huawei.securityserver.IGeographyLocation;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class AntiRelayConnectManager {
    private static final String BIND_ACTION = "com.huawei.securityserver.HwAntirelayService";
    private static final int BIND_MAX_TRY_TIME = 3;
    private static final ExecutorService CALL_JOB = Executors.newSingleThreadExecutor();
    private static final Condition CONDITION = LOCK.newCondition();
    private static final AtomicBoolean IS_BINDED = new AtomicBoolean(false);
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final String SERVER_PAKAGE_NAME = "com.huawei.securityserver";
    private static final String TAG = "AntiRelayConnectManager";
    private static final long TIME_OUT = 1000;
    private static ServiceConnection sConnection = new ServiceConnection() {
        /* class com.huawei.antirelay.AntiRelayConnectManager.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(AntiRelayConnectManager.TAG, "Remote service connected.");
            IGeographyLocation unused = AntiRelayConnectManager.sHwGeoLocalService = IGeographyLocation.Stub.asInterface(service);
            AntiRelayConnectManager.LOCK.lock();
            try {
                AntiRelayConnectManager.IS_BINDED.compareAndSet(false, true);
                AntiRelayConnectManager.CONDITION.signalAll();
            } finally {
                AntiRelayConnectManager.LOCK.unlock();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            IGeographyLocation unused = AntiRelayConnectManager.sHwGeoLocalService = null;
            AntiRelayConnectManager.IS_BINDED.compareAndSet(true, false);
            Log.d(AntiRelayConnectManager.TAG, "Remote service disconnected.");
        }
    };
    private static IGeographyLocation sHwGeoLocalService;

    AntiRelayConnectManager() {
    }

    public static Optional<IGeographyLocation> getRemoteService(Context context) {
        Log.d(TAG, "Get remote service instance.");
        if (context == null) {
            return Optional.empty();
        }
        int bindTimes = 0;
        boolean isConnected = isRemoteServiceConnected(context);
        while (!isConnected && bindTimes <= 3) {
            isConnected = isRemoteServiceConnected(context);
            bindTimes++;
        }
        if (!isConnected || sHwGeoLocalService == null) {
            Log.d(TAG, "Bind remote service error: " + bindTimes);
            return Optional.empty();
        }
        Log.d(TAG, "Bind remote service success.");
        return Optional.of(sHwGeoLocalService);
    }

    public static synchronized void finishService(Context context) {
        synchronized (AntiRelayConnectManager.class) {
            if (context != null) {
                if (IS_BINDED.get()) {
                    sHwGeoLocalService = null;
                    context.unbindService(sConnection);
                    IS_BINDED.compareAndSet(true, false);
                }
            }
        }
    }

    private static boolean isRemoteServiceConnected(Context context) {
        Log.d(TAG, "Bind remote service start.");
        if (sHwGeoLocalService != null) {
            return true;
        }
        if (context == null) {
            return false;
        }
        Log.d(TAG, "isRemoteServiceConnected: " + context);
        boolean result = attemptToBindService(context);
        LOCK.lock();
        try {
            CONDITION.await(TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "LOCK wait InterruptedException " + e.getMessage());
        } catch (Throwable th) {
            LOCK.unlock();
            throw th;
        }
        LOCK.unlock();
        Log.d(TAG, "Bind remote service is: " + result);
        return result;
    }

    private static boolean attemptToBindService(Context context) {
        Intent intent = new Intent();
        intent.setClassName(SERVER_PAKAGE_NAME, BIND_ACTION);
        Log.d(TAG, "Start bind service.");
        try {
            return context.bindService(intent, 1, CALL_JOB, sConnection);
        } catch (SecurityException e) {
            Log.e(TAG, "Get remote service connect fail " + e.getMessage());
            return false;
        }
    }
}
