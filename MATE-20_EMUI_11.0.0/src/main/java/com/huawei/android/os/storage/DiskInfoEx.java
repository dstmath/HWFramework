package com.huawei.android.os.storage;

import android.os.storage.DiskInfo;

public class DiskInfoEx {
    public static final int TYPE_HONOR_EARL = 1;
    public static final int TYPE_HUAWEI_EARL = 0;
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

    public boolean isUsbPartner(int type) {
        return type == 0 ? (this.mDiskInfo.flags & 192) != 0 : type == 1 && (this.mDiskInfo.flags & 128) != 0;
    }
}
