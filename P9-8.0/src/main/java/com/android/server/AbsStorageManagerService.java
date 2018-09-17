package com.android.server;

import android.os.storage.IStorageManager.Stub;

public abstract class AbsStorageManagerService extends Stub {
    public int startClean() {
        return 0;
    }

    public int stopClean() {
        return 0;
    }

    public int getNotificationLevel() {
        return 0;
    }

    public int getUndiscardInfo() {
        return 0;
    }

    public int getMaxTimeCost() {
        return 0;
    }

    public int getMinTimeCost() {
        return 0;
    }

    public int getPercentComplete() {
        return 0;
    }
}
