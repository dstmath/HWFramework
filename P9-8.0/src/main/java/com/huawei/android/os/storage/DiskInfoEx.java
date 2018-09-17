package com.huawei.android.os.storage;

import android.os.storage.DiskInfo;

public class DiskInfoEx {
    private DiskInfo mDiskInfo;

    public DiskInfoEx(DiskInfo info) {
        this.mDiskInfo = info;
    }

    public boolean isSd() {
        return this.mDiskInfo.isSd();
    }

    public boolean isUsb() {
        return this.mDiskInfo.isUsb();
    }
}
