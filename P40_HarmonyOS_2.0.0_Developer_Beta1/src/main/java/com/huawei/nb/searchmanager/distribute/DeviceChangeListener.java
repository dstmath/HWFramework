package com.huawei.nb.searchmanager.distribute;

public interface DeviceChangeListener {
    void onDeviceOffline(DeviceInfo deviceInfo);

    void onDeviceOnline(DeviceInfo deviceInfo);
}
