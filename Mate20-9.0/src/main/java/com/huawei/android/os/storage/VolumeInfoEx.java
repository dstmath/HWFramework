package com.huawei.android.os.storage;

import android.os.storage.VolumeInfo;

public class VolumeInfoEx {
    private VolumeInfo mVolumeInfo;

    public VolumeInfoEx(VolumeInfo info) {
        this.mVolumeInfo = info;
    }

    public DiskInfoEx getDisk() {
        if (this.mVolumeInfo == null) {
            return null;
        }
        return new DiskInfoEx(this.mVolumeInfo.getDisk());
    }
}
