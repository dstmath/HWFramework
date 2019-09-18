package com.huawei.systemmanager.spacecleanner;

import android.content.Context;
import android.os.storage.StorageManager;

public class HwStorageManagerEx {
    private final int INVALID_VALUE = -10;
    private StorageManager mImpl = null;

    public HwStorageManagerEx(Context context) {
        this.mImpl = (StorageManager) context.getSystemService("storage");
    }

    public long getUndiscardInfo() {
        if (this.mImpl == null) {
            return -10;
        }
        return (long) this.mImpl.getUndiscardInfo();
    }

    public int getMinTimeCost() {
        if (this.mImpl == null) {
            return -10;
        }
        return this.mImpl.getMinTimeCost();
    }

    public int startClean() {
        if (this.mImpl == null) {
            return -10;
        }
        return this.mImpl.startClean();
    }

    public int stopClean() {
        if (this.mImpl == null) {
            return -10;
        }
        return this.mImpl.stopClean();
    }

    public int getPercentComplete() {
        if (this.mImpl == null) {
            return -10;
        }
        return this.mImpl.getPercentComplete();
    }

    public int getNotificationLevel() {
        if (this.mImpl == null) {
            return -10;
        }
        return this.mImpl.getNotificationLevel();
    }
}
