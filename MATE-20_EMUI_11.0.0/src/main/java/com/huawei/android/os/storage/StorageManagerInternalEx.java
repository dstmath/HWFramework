package com.huawei.android.os.storage;

import android.os.storage.StorageManagerInternal;
import com.android.server.LocalServices;

public class StorageManagerInternalEx {
    private StorageManagerInternal mStorageManagerInternal;

    public StorageManagerInternalEx() {
        this.mStorageManagerInternal = null;
        this.mStorageManagerInternal = (StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class);
    }

    public int getExternalStorageMountMode(int uid, String packageName) {
        return this.mStorageManagerInternal.getExternalStorageMountMode(uid, packageName);
    }
}
