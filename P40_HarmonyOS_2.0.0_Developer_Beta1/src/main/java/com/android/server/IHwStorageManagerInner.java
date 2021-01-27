package com.android.server;

import android.os.storage.VolumeInfo;

public interface IHwStorageManagerInner {
    VolumeInfo getVolumeInfo(String str);

    void mountAfterCheckCompleted(VolumeInfo volumeInfo);

    void notifyVolumeStateChanged(VolumeInfo volumeInfo, int i, int i2);

    void onCheckStart(String str);
}
