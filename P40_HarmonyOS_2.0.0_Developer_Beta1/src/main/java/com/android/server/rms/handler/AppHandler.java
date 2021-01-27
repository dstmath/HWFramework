package com.android.server.rms.handler;

import android.app.ActivityManager;
import android.app.mtm.MultiTaskPolicy;
import android.content.Context;
import android.content.Intent;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.HwSysResManagerService;

public final class AppHandler implements HwSysResHandler {
    private static final String ACTION_NOTIFY_CRASHINFO_SYSAPP = "com.android.server.rms.handler.AppHandler.ACTION_NOTIFY_CRASHINFO_SYSAPP";
    private static final String PERMISSION_NOTIFY_CRASHINFO_SYSAPP = "com.android.server.rms.handler.AppHandler.PERMISSION_NOTIFY_CRASHINFO_SYSAPP";
    private static final String SEPERATE_STRING = "/";
    private static final String TAG = "RMS.AppHandler";
    private static final int TYPE_CLEAR_DATA = 1;
    private static final int TYPE_DISABLE_APP = 2;
    private static final int TYPE_NOTIFY_CRASHINFO = 3;
    private static final int TYPE_NOTIFY_CRASHINFO_SYSAPP = 4;
    private static AppHandler appHandler;
    private Context mContext;

    private AppHandler(Context context) {
        this.mContext = context;
    }

    public static synchronized AppHandler getInstance(Context context) {
        AppHandler appHandler2;
        synchronized (AppHandler.class) {
            if (appHandler == null) {
                appHandler = new AppHandler(context);
                if (Utils.DEBUG) {
                    Log.d(TAG, "Create new AppHandler");
                }
            }
            appHandler2 = appHandler;
        }
        return appHandler2;
    }

    @Override // com.android.server.rms.handler.HwSysResHandler
    public boolean execute(MultiTaskPolicy policy) {
        if (policy == null) {
            return false;
        }
        int callingUid = policy.getPolicyData().getInt("callingUid");
        String pkg = policy.getPolicyData().getString("pkg");
        if (callingUid <= 0 || pkg == null) {
            return false;
        }
        if (Utils.DEBUG || Log.HWINFO) {
            Log.d(TAG, "execute: pkg " + pkg + " policy " + policy.getPolicy() + " callingUid " + callingUid);
        }
        int policy2 = policy.getPolicy();
        if (policy2 == 1) {
            return clearUserData(pkg);
        }
        if (policy2 == 2) {
            return disableApp(pkg);
        }
        if (policy2 != 3) {
            if (policy2 != 4) {
                return false;
            }
            return notifySysAppCrashInfo(pkg);
        } else if (HwSysResManagerService.self() == null) {
            return false;
        } else {
            HwSysResManagerService.self().dispatchProcessDiedOverload(pkg, callingUid);
            return false;
        }
    }

    @Override // com.android.server.rms.handler.HwSysResHandler
    public void interrupt() {
        if (Utils.DEBUG) {
            Log.i(TAG, "interrupt---");
        }
    }

    private boolean clearUserData(String pkg) {
        Object activityService = this.mContext.getSystemService("activity");
        if (activityService instanceof ActivityManager) {
            return ((ActivityManager) activityService).clearApplicationUserData(pkg, null);
        }
        return false;
    }

    private boolean disableApp(String pkg) {
        this.mContext.getPackageManager().setApplicationEnabledSetting(pkg, 3, 0);
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
        if (!Utils.DEBUG) {
            return true;
        }
        Log.i(TAG, "notifyCrashInfoSysApp send broadcast successfully!");
        return true;
    }
}
