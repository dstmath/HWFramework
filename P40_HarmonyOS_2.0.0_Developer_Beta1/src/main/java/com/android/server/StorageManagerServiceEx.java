package com.android.server;

import android.util.Slog;

public class StorageManagerServiceEx {
    private static final String TAG = "StorageManagerServiceEx";

    public static long getPartitionInfo(String partitionName, int infoType) {
        if (HwStorageManagerService.sSelf != null) {
            return HwStorageManagerService.sSelf.getPartitionInfo(partitionName, infoType);
        }
        Slog.e(TAG, "error getPartitionInfo HwGeneralService NULL PTR");
        return -1;
    }
}
