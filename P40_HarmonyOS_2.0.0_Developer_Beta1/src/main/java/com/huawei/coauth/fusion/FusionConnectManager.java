package com.huawei.coauth.fusion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.huawei.coauth.utils.LogUtils;
import com.huawei.fusionauth.fusion.IFusion;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FusionConnectManager {
    private static final int BIND_MAX_TRY_TIME = 3;
    private static final ExecutorService CALL_JOB = new ScheduledThreadPoolExecutor(1);
    private static final int LATCH_COUNT = 1;
    private static final String SERVER_ACTION = "com.huawei.fusionauth.FusionService";
    private static final String SERVER_COMPONENT = "com.huawei.fusionauth.FusionService";
    private static final String SERVER_PACKAGE = "com.huawei.coauthservice";
    private static final String TAG = "FusionConnectManager";
    private static final long TIME_OUT = 2000;
    private static int bindTimes = 0;
    private static IFusion fusionService = null;
    private static CountDownLatch latch;
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        /* class com.huawei.coauth.fusion.FusionConnectManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            LogUtils.info(FusionConnectManager.TAG, "Service connected.");
            IFusion unused = FusionConnectManager.fusionService = IFusion.Stub.asInterface(binder);
            FusionConnectManager.latch.countDown();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtils.info(FusionConnectManager.TAG, "Service disconnected.");
            IFusion unused = FusionConnectManager.fusionService = null;
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            LogUtils.info(FusionConnectManager.TAG, "Service onBindingDied.");
            IFusion unused = FusionConnectManager.fusionService = null;
        }
    };

    static Optional<IFusion> connectService(Context context) {
        IFusion iFusion;
        LogUtils.info(TAG, "Connect service.");
        boolean isConnected = false;
        while (!isConnected && bindTimes <= 3) {
            isConnected = isServiceConnected(context);
            bindTimes++;
        }
        if (!isConnected || (iFusion = fusionService) == null) {
            LogUtils.error(TAG, "Bind service error, bind times is " + bindTimes);
            bindTimes = 0;
            return Optional.empty();
        }
        bindTimes = 0;
        return Optional.of(iFusion);
    }

    private static boolean isServiceConnected(Context context) {
        LogUtils.info(TAG, "Start to bind service.");
        IFusion iFusion = fusionService;
        if (iFusion == null || iFusion.asBinder() == null || !fusionService.asBinder().isBinderAlive()) {
            fusionService = null;
            if (context == null) {
                LogUtils.error(TAG, "Context is null.");
                return false;
            }
            boolean isConnected = false;
            latch = new CountDownLatch(1);
            Intent intent = new Intent();
            intent.setClassName("com.huawei.coauthservice", "com.huawei.fusionauth.FusionService");
            intent.setAction("com.huawei.fusionauth.FusionService");
            try {
                Context appContext = context.getApplicationContext();
                if (appContext != null) {
                    isConnected = appContext.bindService(intent, 1, CALL_JOB, serviceConnection);
                } else {
                    LogUtils.error(TAG, "Context getApplicationContext is null.");
                }
            } catch (SecurityException e) {
                LogUtils.error(TAG, "Failed to connect to the service.");
            }
            try {
                boolean awaitResult = latch.await(TIME_OUT, TimeUnit.MILLISECONDS);
                LogUtils.info(TAG, "latch own result is " + awaitResult);
            } catch (InterruptedException e2) {
                LogUtils.error(TAG, "CountDownLatch InterruptedException.");
            }
            return isConnected;
        }
        LogUtils.info(TAG, "The service has been connected.");
        return true;
    }
}
