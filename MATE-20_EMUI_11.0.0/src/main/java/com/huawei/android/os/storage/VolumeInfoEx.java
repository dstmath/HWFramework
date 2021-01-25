package com.huawei.android.os.storage;

import android.os.storage.VolumeInfo;

public class VolumeInfoEx {
    private VolumeInfo mVolumeInfo;

    public VolumeInfoEx(VolumeInfo info) {
        this.mVolumeInfo = info;
    }

    public DiskInfoEx getDisk() {
        VolumeInfo volumeInfo = this.mVolumeInfo;
        if (volumeInfo == null) {
            return null;
        }
        return new DiskInfoEx(volumeInfo.getDisk());
    }
}
