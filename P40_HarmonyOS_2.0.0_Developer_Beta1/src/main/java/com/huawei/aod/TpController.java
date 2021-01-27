package com.huawei.aod;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

/* access modifiers changed from: package-private */
public class TpController {
    private static final int SCREEN_OFF = 1;
    private static final int SCREEN_ON = 2;
    private static final String TAG = TpController.class.getSimpleName();
    private static final int TP_FEATURE_SUSPEND = 11;
    private static final int TP_FEATURE_TAP = 6;
    private static final String TP_RESUME = "2";
    private static final String TP_SUSPEND = "1";
    private static final String TP_TAP_OFF = "0";
    private static final String TP_TAP_ON = "1";
    private int mLastAodState = 0;
    private volatile ITouchscreen mTpService = null;
    private final Object mTpServiceLock = new Object();

    TpController() {
    }

    public void sendCommandToTp(int featureFlag, String cmdStr) {
        if (this.mTpService == null) {
            synchronized (this.mTpServiceLock) {
                connectToTpServiceLocked();
            }
        }
        synchronized (this.mTpServiceLock) {
            if (this.mTpService == null) {
                Log.w(TAG, "mTpService is null, return");
                return;
            }
            String str = TAG;
            Log.i(str, "sendCmdToTpHal:\t(feature: " + featureFlag + ",  cmd: " + cmdStr + ")");
            try {
                int result = this.mTpService.hwSetFeatureConfig(featureFlag, cmdStr);
                String str2 = TAG;
                Log.i(str2, "send command result:\t" + result);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to set cmd to tp hal");
            }
        }
    }

    private void connectToTpServiceLocked() {
        try {
            this.mTpService = ITouchscreen.getService();
            if (this.mTpService != null) {
                Log.d(TAG, "get tp service success.");
                this.mTpService.asBinder().linkToDeath(new IHwBinder.DeathRecipient() {
                    /* class com.huawei.aod.$$Lambda$TpController$O64HOuE0_yqazzrCjbn72x2F3o */

                    public final void serviceDied(long j) {
                        TpController.this.lambda$connectToTpServiceLocked$0$TpController(j);
                    }
                }, 0);
                return;
            }
            Log.e(TAG, "get tp service failed");
        } catch (NoSuchElementException e) {
            Log.e(TAG, "tp hal service not found. Did the service fail to start?");
        } catch (RemoteException e2) {
            Log.e(TAG, "tp hal service not responding");
        }
    }

    public /* synthetic */ void lambda$connectToTpServiceLocked$0$TpController(long aw1ExeTime) {
        synchronized (this.mTpServiceLock) {
            Log.e(TAG, "TpService died");
            this.mTpService = null;
        }
    }

    private void enableTapIfNeed(Context context) {
        Log.i(TAG, "Begin to set TP lmt switch");
        ContentResolver resolver = context.getContentResolver();
        int currentUser = ActivityManager.getCurrentUser();
        boolean isFpEnable = false;
        boolean isAodEnable = Settings.Secure.getIntForUser(resolver, "aod_switch", 0, currentUser) == 1;
        if (Settings.Secure.getIntForUser(resolver, "fp_keyguard_enable", 0, currentUser) == 1) {
            isFpEnable = true;
        }
        Log.i(TAG, "Set tp state isAodEnable:" + isAodEnable + "isFpEnable:" + isFpEnable);
        sendCommandToTp(6, (isAodEnable || isFpEnable) ? "1" : "0");
    }

    private void suspendIfNeed(Context context, boolean isSuspend) {
        String cmd;
        ContentResolver resolver = context.getContentResolver();
        int currentUser = ActivityManager.getCurrentUser();
        boolean isFpEnable = false;
        boolean isAodEnable = Settings.Secure.getIntForUser(resolver, "aod_switch", 0, currentUser) == 1;
        if (Settings.Secure.getIntForUser(resolver, "fp_keyguard_enable", 0, currentUser) == 1) {
            isFpEnable = true;
        }
        if (isAodEnable || isFpEnable) {
            cmd = isSuspend ? "1" : "2";
        } else {
            cmd = "0";
        }
        sendCommandToTp(11, cmd);
    }

    public void configTpInApMode(Context context, int aodState) {
        int curAodState;
        if (aodState == 102) {
            curAodState = 2;
        } else {
            curAodState = 1;
        }
        if (curAodState != this.mLastAodState) {
            this.mLastAodState = curAodState;
            if (curAodState == 1) {
                enableTapIfNeed(context);
                suspendIfNeed(context, true);
                return;
            }
            suspendIfNeed(context, false);
        }
    }
}
