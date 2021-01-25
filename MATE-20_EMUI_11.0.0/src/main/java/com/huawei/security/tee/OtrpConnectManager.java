package com.huawei.security.tee;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.huawei.hwpanpayservice.IHwTEEService;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class OtrpConnectManager {
    private static final String BIND_ACTION = "com.huawei.hwpanpayservice.HwTEEService";
    private static final long BIND_MAX_TRY_TIME = 3;
    private static final ExecutorService CALL_JOB = Executors.newSingleThreadExecutor();
    private static final int FAIL = -1;
    private static final Object LOCK = new Object();
    private static final String SERVER_PAKAGE_NAME = "com.huawei.hwpanpayservice";
    private static final int SUCCESS = 0;
    private static final String TAG = "OtrpConnectManager";
    private static final long TIME_OUT = 500;
    private static final int WRONG_PARAMETER = -2;
    private static ServiceConnection sConnection = new ServiceConnection() {
        /* class com.huawei.security.tee.OtrpConnectManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(OtrpConnectManager.TAG, "Remote service connected.");
            IHwTEEService unused = OtrpConnectManager.sHwTEEService = IHwTEEService.Stub.asInterface(service);
            synchronized (OtrpConnectManager.LOCK) {
                boolean unused2 = OtrpConnectManager.sFake = true;
                OtrpConnectManager.LOCK.notifyAll();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IHwTEEService unused = OtrpConnectManager.sHwTEEService = null;
            Log.d(OtrpConnectManager.TAG, "Remote service disconnected.");
        }
    };
    private static boolean sFake = false;
    private static IHwTEEService sHwTEEService;

    private OtrpConnectManager() {
    }

    public static Optional<IHwTEEService> getRemoteService(Context context) {
        Log.d(TAG, "Get remote service instance.");
        if (context == null) {
            Log.d(TAG, "Get remote service fail.");
            return Optional.empty();
        }
        int bindTimes = 0;
        boolean isConnected = false;
        while (!isConnected && ((long) bindTimes) <= BIND_MAX_TRY_TIME) {
            isConnected = isRemoteServiceConnected(context);
            bindTimes++;
        }
        if (isConnected) {
            Log.d(TAG, "Bind remote service success.");
            return Optional.of(sHwTEEService);
        }
        Log.d(TAG, "Bind remote service error: " + bindTimes);
        return Optional.empty();
    }

    public static synchronized int unBindHwTeeService(Context context) {
        synchronized (OtrpConnectManager.class) {
            if (context == null) {
                Log.e(TAG, "Context is null.");
                return -2;
            } else if (sHwTEEService != null) {
                context.unbindService(sConnection);
                sHwTEEService = null;
                Log.d(TAG, "unBind HwTeeService is success.");
                return 0;
            } else {
                Log.d(TAG, "unBind HwTeeService is failed.");
                return -1;
            }
        }
    }

    private static boolean isRemoteServiceConnected(Context context) {
        Log.d(TAG, "Bind remote service start.");
        if (sHwTEEService != null) {
            return true;
        }
        if (context == null) {
            Log.d(TAG, "Context is null.");
            return false;
        }
        boolean result = attemptToBindService(context);
        synchronized (LOCK) {
            try {
                LOCK.wait(TIME_OUT);
            } catch (InterruptedException e) {
                Log.e(TAG, "LOCK wait InterruptedException.");
            }
        }
        Log.d(TAG, "Bind remote service is: " + result);
        return result;
    }

    private static boolean attemptToBindService(Context context) {
        if (context == null) {
            Log.d(TAG, "Context is null.");
            return false;
        }
        Intent intent = new Intent();
        intent.setClassName(SERVER_PAKAGE_NAME, BIND_ACTION);
        Log.d(TAG, "Start bind service.");
        try {
            return context.bindService(intent, 1, CALL_JOB, sConnection);
        } catch (SecurityException e) {
            Log.e(TAG, "Get remote service connect fail.");
            return false;
        }
    }
}
