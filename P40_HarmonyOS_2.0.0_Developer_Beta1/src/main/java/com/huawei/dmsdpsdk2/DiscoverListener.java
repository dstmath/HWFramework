package com.huawei.dmsdpsdk2;

import java.util.Map;

public interface DiscoverListener {
    void onDeviceFound(DMSDPDevice dMSDPDevice);

    void onDeviceLost(DMSDPDevice dMSDPDevice);

    void onDeviceUpdate(DMSDPDevice dMSDPDevice, int i);

    void onStateChanged(int i, Map<String, Object> map);
}
