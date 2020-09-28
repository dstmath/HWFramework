package com.huawei.android.os.storage;

import android.os.storage.StorageVolume;

public class StorageVolumeEx {
    public static final String getPath(StorageVolume storageVolume) {
        return storageVolume.getPath();
    }

    public static final String getInternalPath(StorageVolume storageVolume) {
        return storageVolume.getInternalPath();
    }
}
