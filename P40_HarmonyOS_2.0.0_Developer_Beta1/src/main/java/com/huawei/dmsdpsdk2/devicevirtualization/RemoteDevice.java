package com.huawei.dmsdpsdk2.devicevirtualization;

public class RemoteDevice {
    private String mDeviceId;
    private String mDeviceName;

    public RemoteDevice(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }
}
