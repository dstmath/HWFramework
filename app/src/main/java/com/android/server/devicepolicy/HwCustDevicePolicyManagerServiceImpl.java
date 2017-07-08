package com.android.server.devicepolicy;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Slog;

public class HwCustDevicePolicyManagerServiceImpl extends HwCustDevicePolicyManagerService {
    private static final boolean IS_WIPE_STORAGE_DATA;
    private static final String LOG_TAG = "HwCustDevicePolicyManagerServiceImpl";

    static {
        IS_WIPE_STORAGE_DATA = SystemProperties.getBoolean("ro.config.hw_eas_sdformat", IS_WIPE_STORAGE_DATA);
    }

    public void wipeStorageData(Context context) {
        if (IS_WIPE_STORAGE_DATA && context != null) {
            StorageManager mStorageManager = (StorageManager) context.getSystemService("storage");
            for (VolumeInfo vol : mStorageManager.getVolumes()) {
                if (vol.getDisk() != null && vol.getDisk().isSd()) {
                    mStorageManager.partitionPublic(vol.getDisk().getId());
                    break;
                }
            }
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.addFlags(268435456);
            intent.putExtra("masterClearWipeDataFactory", true);
            context.sendBroadcast(intent);
        }
    }

    public boolean wipeDataAndReset(Context context) {
        if (context == null) {
            return IS_WIPE_STORAGE_DATA;
        }
        if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_factory_reset")) {
            Slog.w(LOG_TAG, "Remote Wiping data is not allowed for this user.");
            return IS_WIPE_STORAGE_DATA;
        }
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(268435456);
        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        context.sendBroadcast(intent);
        return true;
    }
}
