package com.huawei.nb.searchmanager.distribute;

import com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback;
import java.util.Objects;

public class DeviceChangeCallback extends IDeviceChangeCallback.Stub {
    private DeviceChangeListener listener;

    public DeviceChangeCallback(DeviceChangeListener deviceChangeListener) {
        Objects.requireNonNull(deviceChangeListener, "Device change listener cannot be null");
        this.listener = deviceChangeListener;
    }

    @Override // com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback
    public void onDeviceOnline(DeviceInfo deviceInfo) {
        this.listener.onDeviceOnline(deviceInfo);
    }

    @Override // com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback
    public void onDeviceOffline(DeviceInfo deviceInfo) {
        this.listener.onDeviceOffline(deviceInfo);
    }
}
