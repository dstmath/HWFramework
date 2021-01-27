package com.huawei.trustedthingsauth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.trustedthingsauth.ITrustedThings;
import com.huawei.trustedthingsauth.TrustedThings;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TrustedThingsServiceManager {
    private static final String BIND_ACTION = "com.huawei.trustedthingsauth.HwTrustThingsService";
    private static final int BIND_MAX_TRY_TIME = 3;
    private static final ExecutorService CALL_BACK_JOB = new ThreadPoolExecutor(1, 1, THREAD_TIME_OUT, TimeUnit.SECONDS, new LinkedBlockingDeque());
    private static final int CORE_THREAD_SIZE = 1;
    private static final int LATCH_COUNT = 1;
    private static final int MAX_THREAD_SIZE = 1;
    private static final String SERVER_PACKAGE_NAME = "com.huawei.trustedthingsauth";
    private static final int SERVICE_DIED = 9999;
    private static final String TAG = "TrustedThingsServiceManager";
    private static final long THREAD_TIME_OUT = 10;
    private static final long TIME_OUT = 2000;
    private static int bindTimes = 0;
    private static CountDownLatch latch;
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        /* class com.huawei.trustedthingsauth.TrustedThingsServiceManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            LogUtil.info(TrustedThingsServiceManager.TAG, "Service connected.");
            ITrustedThings unused = TrustedThingsServiceManager.trustedThingsService = ITrustedThings.Stub.asInterface(binder);
            TrustedThingsServiceManager.latch.countDown();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.info(TrustedThingsServiceManager.TAG, "Service disconnected.");
            ITrustedThings unused = TrustedThingsServiceManager.trustedThingsService = null;
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            LogUtil.info(TrustedThingsServiceManager.TAG, "Service onBindingDied.");
            ITrustedThings unused = TrustedThingsServiceManager.trustedThingsService = null;
            Map<String, TrustedThings.HwTrustedThingsCallback> trustedThingsCallbackMap = TrustedThings.getTrustedThingsCallbackMap();
            if (trustedThingsCallbackMap != null) {
                for (TrustedThings.HwTrustedThingsCallback callback : trustedThingsCallbackMap.values()) {
                    if (callback != null) {
                        try {
                            callback.onResult(TrustedThingsServiceManager.SERVICE_DIED);
                        } catch (RemoteException e) {
                            LogUtil.error(TrustedThingsServiceManager.TAG, "Call trustedThingsCallback onResult RemoteException.");
                        }
                    }
                }
                trustedThingsCallbackMap.clear();
            }
        }
    };
    private static ITrustedThings trustedThingsService = null;

    public static Optional<ITrustedThings> connectService(Context context) {
        if (context == null) {
            LogUtil.error(TAG, "Connect service context is null.");
            return Optional.empty();
        } else if (!isTrustedThingsPackageExist(context)) {
            LogUtil.error(TAG, "TrustedThings package does not exist.");
            return Optional.empty();
        } else {
            LogUtil.debug(TAG, "Connect service.");
            boolean isConnected = false;
            while (!isConnected && bindTimes <= 3) {
                isConnected = isServiceConnected(context);
                bindTimes++;
            }
            if (isConnected) {
                bindTimes = 0;
                return Optional.of(trustedThingsService);
            }
            LogUtil.error(TAG, "Bind service error, bind times is " + bindTimes);
            bindTimes = 0;
            return Optional.empty();
        }
    }

    public static void disconnectService(Context context) {
        if (context == null || serviceConnection == null) {
            LogUtil.error(TAG, "Disconnect service context or serviceConnection is null.");
            return;
        }
        LogUtil.debug(TAG, "Disconnecting from the service.");
        try {
            context.unbindService(serviceConnection);
            trustedThingsService = null;
            bindTimes = 0;
        } catch (SecurityException e) {
            LogUtil.error(TAG, "Service unbind exception.");
        }
    }

    private static boolean isServiceConnected(Context context) {
        LogUtil.debug(TAG, "Start to bind service.");
        if (trustedThingsService != null) {
            LogUtil.debug(TAG, "The service has been connected.");
            return true;
        }
        boolean isConnected = false;
        latch = new CountDownLatch(1);
        Intent intent = new Intent();
        intent.setClassName(SERVER_PACKAGE_NAME, BIND_ACTION);
        try {
            isConnected = context.getApplicationContext().bindService(intent, 1, CALL_BACK_JOB, serviceConnection);
        } catch (SecurityException e) {
            LogUtil.error(TAG, "Failed to connect to the service.");
        }
        try {
            latch.await(TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e2) {
            LogUtil.error(TAG, "CountDownLatch InterruptedException.");
        }
        return isConnected;
    }

    private static boolean isTrustedThingsPackageExist(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(SERVER_PACKAGE_NAME, 0);
            if (info != null) {
                return isSystemApp(info);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.error(TAG, "NameNotFoundException.");
        }
        return false;
    }

    private static boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }
}
