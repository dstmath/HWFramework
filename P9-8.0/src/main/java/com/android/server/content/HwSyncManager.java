package com.android.server.content;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.util.Log;

public final class HwSyncManager extends SyncManager {
    private static final String gmsPkg = "com.google.android.gms";
    private final PackageManager mPackageManager;
    private final PowerManager mPowerManager;

    public HwSyncManager(Context context, boolean factoryTest) {
        super(context, factoryTest);
        this.mPackageManager = context.getPackageManager();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    private boolean doesUidHavePackage(int uid, String packageName) {
        if (packageName == null) {
            return false;
        }
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames == null) {
            return false;
        }
        Log.d("SyncManager", "doesUidHavePackage: uid = " + uid);
        for (String name : packageNames) {
            if (name.contains(packageName)) {
                return true;
            }
        }
        return false;
    }
}
