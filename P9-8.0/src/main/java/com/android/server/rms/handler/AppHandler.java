package com.android.server.rms.handler;

import android.app.ActivityManager;
import android.app.mtm.MultiTaskPolicy;
import android.content.Context;
import android.content.Intent;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.rms.HwSysResManagerService;

public final class AppHandler implements HwSysResHandler {
    private static final String ACTION_NOTIFY_CRASHINFO_SYSAPP = "com.android.server.rms.handler.AppHandler.ACTION_NOTIFY_CRASHINFO_SYSAPP";
    private static final String PERMISSION_NOTIFY_CRASHINFO_SYSAPP = "com.android.server.rms.handler.AppHandler.PERMISSION_NOTIFY_CRASHINFO_SYSAPP";
    private static final String TAG = "RMS.AppHandler";
    private static final int TYPE_CLEAR_DATA = 1;
    private static final int TYPE_DISABLE_APP = 2;
    private static final int TYPE_NOTIFY_CRASHINFO = 3;
    private static final int TYPE_NOTIFY_CRASHINFO_SYSAPP = 4;
    private static AppHandler mAppHandler;
    private Context mContext;

    private AppHandler(Context context) {
        this.mContext = context;
    }

    public static synchronized AppHandler getInstance(Context context) {
        AppHandler appHandler;
        synchronized (AppHandler.class) {
            if (mAppHandler == null) {
                mAppHandler = new AppHandler(context);
                if (Utils.DEBUG) {
                    Log.d(TAG, "Create new AppHandler");
                }
            }
            appHandler = mAppHandler;
        }
        return appHandler;
    }

    public boolean execute(MultiTaskPolicy policy) {
        boolean result = false;
        if (policy == null) {
            return false;
        }
        int callingUid = policy.getPolicyData().getInt("callingUid");
        String pkg = policy.getPolicyData().getString(HwGpsPowerTracker.DEL_PKG);
        if (callingUid <= 0 || pkg == null) {
            return false;
        }
        if (Utils.DEBUG || Log.HWINFO) {
            Log.d(TAG, "execute: pkg " + pkg + " policy " + policy.getPolicy());
        }
        switch (policy.getPolicy()) {
            case 1:
                result = clearUserData(callingUid, pkg);
                break;
            case 2:
                result = disableApp(callingUid, pkg);
                break;
            case 3:
                HwSysResManagerService.self().dispatchProcessDiedOverload(pkg, callingUid);
                break;
            case 4:
                result = notifySysAppCrashInfo(pkg);
                break;
            default:
                if (Utils.DEBUG) {
                    Log.i(TAG, "Do not execute any operation, since there is no matching policy");
                    break;
                }
                break;
        }
        return result;
    }

    public void interrupt() {
        if (Utils.DEBUG) {
            Log.i(TAG, "interrupt---");
        }
    }

    private boolean clearUserData(int callingUid, String pkg) {
        boolean result = ((ActivityManager) this.mContext.getSystemService("activity")).clearApplicationUserData(pkg, null);
        if (result) {
            Log.i(TAG, "Successfully clearUserData of " + pkg + "/" + callingUid);
        } else {
            Log.i(TAG, "Failed to clearUserData of " + pkg + "/" + callingUid);
        }
        return result;
    }

    private boolean disableApp(int callingUid, String pkg) {
        this.mContext.getPackageManager().setApplicationEnabledSetting(pkg, 3, 0);
        Log.i(TAG, "Successfully disableApp of " + pkg + "/" + callingUid);
        return true;
    }

    private boolean notifySysAppCrashInfo(String pkg) {
        if (pkg == null) {
            return false;
        }
        Intent intent = new Intent(ACTION_NOTIFY_CRASHINFO_SYSAPP);
        intent.addFlags(1073741824);
        intent.putExtra("android.intent.extra.PACKAGES", pkg);
        this.mContext.sendBroadcast(intent, PERMISSION_NOTIFY_CRASHINFO_SYSAPP);
        if (Utils.DEBUG) {
            Log.i(TAG, "notifyCrashInfoSysApp send broadcast successfully!");
        }
        return true;
    }
}
