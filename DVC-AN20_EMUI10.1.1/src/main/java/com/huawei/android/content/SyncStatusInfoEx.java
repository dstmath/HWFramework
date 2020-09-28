package com.huawei.android.content;

import android.content.SyncStatusInfo;

public class SyncStatusInfoEx {
    public SyncStatusInfo mSyncStatusInfo;

    public SyncStatusInfoEx(SyncStatusInfo other) {
        this.mSyncStatusInfo = other;
    }

    public long getLastSuccessTime() {
        SyncStatusInfo syncStatusInfo = this.mSyncStatusInfo;
        if (syncStatusInfo == null) {
            return -1;
        }
        return syncStatusInfo.lastSuccessTime;
    }
}
