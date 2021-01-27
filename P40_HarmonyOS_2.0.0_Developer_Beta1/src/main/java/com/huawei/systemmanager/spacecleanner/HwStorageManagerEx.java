package com.huawei.systemmanager.spacecleanner;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.storage.HwStorageManager;
import android.os.storage.StorageManager;
import android.util.Slog;
import com.huawei.android.app.ERecovery;

public class HwStorageManagerEx {
    private static final String TAG = "HwStorageManagerEx";
    private final int INVALID_VALUE = -10;
    private Context mContext;
    private StorageManager mImpl = null;

    public HwStorageManagerEx(Context context) {
        this.mImpl = (StorageManager) context.getSystemService("storage");
        this.mContext = context;
    }

    public long getUndiscardInfo() {
        StorageManager storageManager = this.mImpl;
        if (storageManager == null) {
            return -10;
        }
        return (long) storageManager.getUndiscardInfo();
    }

    public int getMinTimeCost() {
        StorageManager storageManager = this.mImpl;
        if (storageManager == null) {
            return -10;
        }
        return storageManager.getMinTimeCost();
    }

    public int startClean() {
        StorageManager storageManager = this.mImpl;
        if (storageManager == null) {
            return -10;
        }
        return storageManager.startClean();
    }

    public int stopClean() {
        StorageManager storageManager = this.mImpl;
        if (storageManager == null) {
            return -10;
        }
        return storageManager.stopClean();
    }

    public int getPercentComplete() {
        StorageManager storageManager = this.mImpl;
        if (storageManager == null) {
            return -10;
        }
        return storageManager.getPercentComplete();
    }

    public int getNotificationLevel() {
        StorageManager storageManager = this.mImpl;
        if (storageManager == null) {
            return -10;
        }
        return storageManager.getNotificationLevel();
    }

    public int startTurboZoneAdaptation() {
        if (!isPengineSupport()) {
            return -1;
        }
        try {
            return HwStorageManager.getService().startTurboZoneAdaptation();
        } catch (RemoteException e) {
            return -10;
        }
    }

    public int stopTurboZoneAdaptation() {
        try {
            return HwStorageManager.getService().stopTurboZoneAdaptation();
        } catch (RemoteException e) {
            return -10;
        }
    }

    private boolean isPengineAvailable() {
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo("com.huawei.pengine", 8);
            if (packageInfo == null) {
                return false;
            }
            ProviderInfo[] providerInfos = packageInfo.providers;
            if (providerInfos != null) {
                if (providerInfos.length != 0) {
                    return true;
                }
            }
            Slog.e(TAG, "TurboZone: Pengine provider is not found.");
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "TurboZone: Pengine PackageManager.NameNotFoundException");
            return false;
        }
    }

    private boolean isPengineSupport() {
        if (!isPengineAvailable()) {
            return false;
        }
        Uri uri = Uri.parse("content://com.huawei.pengine.UserProfileProvider");
        ContentResolver resolver = this.mContext.getContentResolver();
        if (resolver == null) {
            return false;
        }
        try {
            Bundle resultBundle = resolver.call(uri, "isSupport", (String) null, (Bundle) null);
            if (resultBundle != null) {
                return resultBundle.getBoolean(ERecovery.RESULT, true);
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
