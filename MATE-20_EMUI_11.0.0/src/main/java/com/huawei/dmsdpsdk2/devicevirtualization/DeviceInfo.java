package com.huawei.dmsdpsdk2.devicevirtualization;

public class DeviceInfo {
    public static final int DEVICE_TYPE_TV = 3;
    public static final int DEVICE_TYPE_VOICEBOX = 6;
    private boolean isIgnoreAndroidCamera;
    private String mDeviceName;
    private int mDeviceType;
    private AudioCapabilities mMicCapabilities;
    private AudioCapabilities mSpeakerCapabilities;

    /* access modifiers changed from: package-private */
    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    /* access modifiers changed from: package-private */
    public int getDeviceType() {
        return this.mDeviceType;
    }

    public void setDeviceType(int deviceType) {
        this.mDeviceType = deviceType;
    }

    /* access modifiers changed from: package-private */
    public boolean isIgnoreAndroidCamera() {
        return this.isIgnoreAndroidCamera;
    }

    public void setIgnoreAndroidCamera(boolean isIgnore) {
        this.isIgnoreAndroidCamera = isIgnore;
    }

    public void setMicCapabilities(AudioCapabilities micCapabilities) {
        this.mMicCapabilities = micCapabilities;
    }

    /* access modifiers changed from: package-private */
    public AudioCapabilities getMicCapabilities() {
        return this.mMicCapabilities;
    }

    public void setSpeakerCapabilities(AudioCapabilities speakerCapabilities) {
        this.mSpeakerCapabilities = speakerCapabilities;
    }

    /* access modifiers changed from: package-private */
    public AudioCapabilities getSpeakerCapabilities() {
        return this.mSpeakerCapabilities;
    }
}
