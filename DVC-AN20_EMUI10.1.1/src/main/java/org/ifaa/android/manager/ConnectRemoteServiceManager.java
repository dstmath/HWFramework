package org.ifaa.android.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.huawei.hwpanpayservice.IHwIFAAService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConnectRemoteServiceManager {
    private static final String BIND_ACTION = "com.huawei.hwpanpayservice.HwIFAAService";
    private static final long BIND_MAX_TRY_TIME = 3;
    private static final ExecutorService CALL_BACK_JOB = Executors.newSingleThreadExecutor();
    private static final Object LOCK = new Object();
    private static final String SERVER_PAKAGE_NAME = "com.huawei.hwpanpayservice";
    private static final String TAG = "ConnectRemoteServiceManager_HwIFAAService";
    private static final long TIME_OUT = 500;
    private static long sBindTimes = 0;
    private static ServiceConnection sConnection = new ServiceConnection() {
        /* class org.ifaa.android.manager.ConnectRemoteServiceManager.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(ConnectRemoteServiceManager.TAG, "Remote service connected.");
            IHwIFAAService unused = ConnectRemoteServiceManager.sHwIFAAService = IHwIFAAService.Stub.asInterface(service);
            synchronized (ConnectRemoteServiceManager.LOCK) {
                boolean unused2 = ConnectRemoteServiceManager.sFake = true;
                ConnectRemoteServiceManager.LOCK.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            IHwIFAAService unused = ConnectRemoteServiceManager.sHwIFAAService = null;
            Log.i(ConnectRemoteServiceManager.TAG, "Remote service disconnected.");
        }
    };
    private static Context sContext = null;
    private static boolean sFake = false;
    private static IHwIFAAService sHwIFAAService = null;

    ConnectRemoteServiceManager() {
    }

    public static void initRemoteService(Context context) {
        Log.i(TAG, "Init Remote service start.");
        sContext = context;
        sBindTimes = 0;
    }

    private static boolean isRemoteServiceConnected() {
        Log.i(TAG, "Bind remote service start.");
        if (sHwIFAAService != null) {
            return true;
        }
        boolean result = attemptToBindService();
        synchronized (LOCK) {
            try {
                LOCK.wait(TIME_OUT);
            } catch (InterruptedException e) {
                Log.e(TAG, "LOCK.wait InterruptedException.");
            }
        }
        if (result) {
            return true;
        }
        Log.i(TAG, "Bind remote service failed.");
        return false;
    }

    public static IHwIFAAService getRemoteServiceInstance() {
        Log.i(TAG, "Get remote service instance.");
        boolean isConnected = false;
        while (!isConnected && sBindTimes <= BIND_MAX_TRY_TIME) {
            isConnected = isRemoteServiceConnected();
            sBindTimes++;
        }
        if (isConnected) {
            return sHwIFAAService;
        }
        Log.i(TAG, "Bind remote service error: " + sBindTimes);
        sBindTimes = 0;
        return null;
    }

    private static boolean attemptToBindService() {
        if (sContext == null) {
            Log.d(TAG, "Context is null.");
            return false;
        }
        Intent intent = new Intent();
        intent.setClassName(SERVER_PAKAGE_NAME, BIND_ACTION);
        try {
            return sContext.bindService(intent, 1, CALL_BACK_JOB, sConnection);
        } catch (SecurityException e) {
            Log.e(TAG, "Get remote service connect fail.");
            return false;
        }
    }

    public static void dealInitRemoteService() {
        Log.i(TAG, "UnBind Remote Service.");
        try {
            if (sContext != null && sConnection != null) {
                sContext.unbindService(sConnection);
                sConnection = null;
                sHwIFAAService = null;
                sBindTimes = 0;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Remote service release exception.");
        }
    }
}
