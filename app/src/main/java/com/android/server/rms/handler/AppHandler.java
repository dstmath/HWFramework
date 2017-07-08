package com.android.server.rms.handler;

import android.app.ActivityManager;
import android.app.mtm.MultiTaskPolicy;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.rms.HwSysResManagerService;

public final class AppHandler implements HwSysResHandler {
    private static final String ACTION_NOTIFY_CRASHINFO_SYSAPP = "com.android.server.rms.handler.AppHandler.ACTION_NOTIFY_CRASHINFO_SYSAPP";
    private static final boolean DEBUG = false;
    private static final String PERMISSION_NOTIFY_CRASHINFO_SYSAPP = "com.android.server.rms.handler.AppHandler.PERMISSION_NOTIFY_CRASHINFO_SYSAPP";
    private static final String TAG = "RMS.AppHandler";
    private static final int TYPE_CLEAR_DATA = 1;
    private static final int TYPE_DISABLE_APP = 2;
    private static final int TYPE_NOTIFY_CRASHINFO = 3;
    private static final int TYPE_NOTIFY_CRASHINFO_SYSAPP = 4;
    private static AppHandler mAppHandler;
    private Context mContext;

    public AppHandler(Context context) {
        this.mContext = context;
    }

    public static synchronized AppHandler getInstance(Context context) {
        AppHandler appHandler;
        synchronized (AppHandler.class) {
            if (mAppHandler == null) {
                mAppHandler = new AppHandler(context);
            }
            appHandler = mAppHandler;
        }
        return appHandler;
    }

    public boolean execute(MultiTaskPolicy policy) {
        boolean result = DEBUG;
        if (policy == null) {
            return DEBUG;
        }
        int callingUid = policy.getPolicyData().getInt("callingUid");
        String pkg = policy.getPolicyData().getString(HwGpsPowerTracker.DEL_PKG);
        if (callingUid <= 0 || pkg == null) {
            return DEBUG;
        }
        if (Log.HWINFO) {
            Log.d(TAG, "execute: pkg " + pkg + " policy " + policy.getPolicy());
        }
        switch (policy.getPolicy()) {
            case TYPE_CLEAR_DATA /*1*/:
                result = clearUserData(callingUid, pkg);
                break;
            case TYPE_DISABLE_APP /*2*/:
                result = disableApp(callingUid, pkg);
                break;
            case TYPE_NOTIFY_CRASHINFO /*3*/:
                HwSysResManagerService.self().dispatchProcessDiedOverload(pkg, callingUid);
                break;
            case TYPE_NOTIFY_CRASHINFO_SYSAPP /*4*/:
                result = notifySysAppCrashInfo(pkg);
                break;
        }
        return result;
    }

    public void interrupt() {
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
        this.mContext.getPackageManager().setApplicationEnabledSetting(pkg, TYPE_NOTIFY_CRASHINFO, 0);
        Log.i(TAG, "Successfully disableApp of " + pkg + "/" + callingUid);
        return true;
    }

    private boolean notifySysAppCrashInfo(String pkg) {
        if (pkg == null) {
            return DEBUG;
        }
        Intent intent = new Intent(ACTION_NOTIFY_CRASHINFO_SYSAPP);
        intent.addFlags(1073741824);
        intent.putExtra("android.intent.extra.PACKAGES", pkg);
        this.mContext.sendBroadcast(intent, PERMISSION_NOTIFY_CRASHINFO_SYSAPP);
        return true;
    }
}
