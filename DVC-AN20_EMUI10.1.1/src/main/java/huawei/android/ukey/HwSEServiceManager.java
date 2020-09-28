package huawei.android.ukey;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.hwpanpayservice.IHwSEService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HwSEServiceManager {
    private static final String BIND_ACTION = "com.huawei.hwpanpayservice.HwSEService";
    private static final long BIND_MAX_TRY_TIME = 3;
    private static final ExecutorService CALL_BACK_JOB = Executors.newSingleThreadExecutor();
    private static final Object LOCK = new Object();
    private static final String SERVER_PAKAGE_NAME = "com.huawei.hwpanpayservice";
    private static final String TAG = "ConnectRemoteServiceManager_HwSEService";
    private static final long TIME_OUT = 500;
    private static long sBindTimes = 0;
    private static ServiceConnection sConnection = new ServiceConnection() {
        /* class huawei.android.ukey.HwSEServiceManager.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwSEServiceManager.TAG, "Remote service connected.");
            IHwSEService unused = HwSEServiceManager.sHwSEService = IHwSEService.Stub.asInterface(service);
            synchronized (HwSEServiceManager.LOCK) {
                boolean unused2 = HwSEServiceManager.sFake = true;
                HwSEServiceManager.LOCK.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            IHwSEService unused = HwSEServiceManager.sHwSEService = null;
            Log.d(HwSEServiceManager.TAG, "Remote service disconnected.");
        }
    };
    private static Context sContext = null;
    private static boolean sFake = false;
    private static IHwSEService sHwSEService = null;

    public static void initRemoteService(@NonNull Context context) {
        Log.d(TAG, "Init Remote service start.");
        sContext = context;
        sBindTimes = 0;
    }

    private static boolean isRemoteServiceConnected() {
        Log.d(TAG, "Bind remote service start.");
        if (sHwSEService != null) {
            return true;
        }
        boolean result = attemptToBindService();
        synchronized (LOCK) {
            try {
                LOCK.wait(TIME_OUT);
            } catch (InterruptedException e) {
                Log.e(TAG, "LOCK.wait InterruptedException." + e);
            }
        }
        if (result) {
            return true;
        }
        Log.d(TAG, "Bind remote service failed.");
        return false;
    }

    public static IHwSEService getRemoteServiceInstance() {
        Log.d(TAG, "Get remote service instance.");
        boolean isConnected = false;
        while (!isConnected && sBindTimes <= BIND_MAX_TRY_TIME) {
            isConnected = isRemoteServiceConnected();
            sBindTimes++;
        }
        if (isConnected) {
            return sHwSEService;
        }
        Log.d(TAG, "Bind remote service error: " + sBindTimes);
        sBindTimes = 0;
        return null;
    }

    private static boolean attemptToBindService() {
        Log.d(TAG, "Attempt to bind remote service instance.");
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
        Log.d(TAG, "UnBind Remote Service.");
        try {
            if (sContext != null && sConnection != null) {
                sContext.unbindService(sConnection);
                sHwSEService = null;
                sBindTimes = 0;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Remote service release exception.");
        }
    }
}
