package com.huawei.android.os.storage;

import android.os.storage.StorageManager;

public class StorageManagerExt {
    public static boolean isUserKeyUnlocked(int userId) {
        return StorageManager.isUserKeyUnlocked(userId);
    }
}
