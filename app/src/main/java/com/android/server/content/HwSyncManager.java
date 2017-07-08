package com.android.server.content;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.pfw.HwPFWService;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public final class HwSyncManager extends SyncManager {
    private static final String gmsPkg = "com.google.android.gms";
    private final PackageManager mPackageManager;
    private final PowerManager mPowerManager;

    public HwSyncManager(Context context, boolean factoryTest) {
        super(context, factoryTest);
        this.mPackageManager = context.getPackageManager();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    public boolean isAllow2Sync(int uid) {
        boolean isOK = true;
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            isOK = pfwService.isGoogleConnectOK();
        }
        if (isOK || !"CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG)) || this.mPowerManager.isScreenOn() || !doesUidHavePackage(uid, gmsPkg)) {
            return true;
        }
        Log.d("SyncManager", "not allowed gms to sync by pfw. ");
        return false;
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

    public boolean checkShouldFilterSync(Intent intent, int userId) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventSyncService(intent, userId);
        }
        return false;
    }
}
