package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPDevice;

public class HiPlayDevice {
    private String deviceId;
    private String deviceName;
    private int deviceType;

    public HiPlayDevice() {
        this.deviceId = BuildConfig.FLAVOR;
        this.deviceType = -1;
        this.deviceName = BuildConfig.FLAVOR;
    }

    public HiPlayDevice(DMSDPDevice dmsdpDevice) {
        this.deviceId = dmsdpDevice.getDeviceId();
        this.deviceType = dmsdpDevice.getDeviceType();
        this.deviceName = dmsdpDevice.getDeviceName();
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public int getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(int deviceType2) {
        this.deviceType = deviceType2;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName2) {
        this.deviceName = deviceName2;
    }
}
