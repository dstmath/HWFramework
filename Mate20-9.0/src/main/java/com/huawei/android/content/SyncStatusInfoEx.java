package com.huawei.android.content;

import android.content.SyncStatusInfo;

public class SyncStatusInfoEx {
    public SyncStatusInfo mSyncStatusInfo;

    public SyncStatusInfoEx(SyncStatusInfo other) {
        this.mSyncStatusInfo = other;
    }

    public long getLastSuccessTime() {
        if (this.mSyncStatusInfo == null) {
            return -1;
        }
        return this.mSyncStatusInfo.lastSuccessTime;
    }
}
