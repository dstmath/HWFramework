package com.android.server;

import android.os.storage.VolumeInfo;
import android.util.ArrayMap;

public class HwCustMountService {
    public boolean isSdInstallEnabled() {
        return false;
    }

    public void notifyPmsUpdate(VolumeInfo vol, int newState) {
    }

    public void physicalWarnOnNotMounted(ArrayMap<String, VolumeInfo> arrayMap, NativeDaemonConnector Connector) {
    }

    public boolean isSdVol(VolumeInfo vol) {
        return false;
    }
}
