package com.android.server.usb;

public final class UsbAudioDevice {
    protected static final boolean DEBUG = false;
    private static final String TAG = "UsbAudioDevice";
    public static final int kAudioDeviceClassMask = 16777215;
    public static final int kAudioDeviceClass_External = 2;
    public static final int kAudioDeviceClass_Internal = 1;
    public static final int kAudioDeviceClass_Undefined = 0;
    public static final int kAudioDeviceMetaMask = -16777216;
    public static final int kAudioDeviceMeta_Alsa = Integer.MIN_VALUE;
    public final int mCard;
    public final int mDevice;
    public final int mDeviceClass;
    private String mDeviceDescription = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private String mDeviceName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    public final boolean mHasCapture;
    public final boolean mHasPlayback;
    public boolean mIsInputHeadset = false;
    public boolean mIsOutputHeadset = false;

    public UsbAudioDevice(int card, int device, boolean hasPlayback, boolean hasCapture, int deviceClass) {
        this.mCard = card;
        this.mDevice = device;
        this.mHasPlayback = hasPlayback;
        this.mHasCapture = hasCapture;
        this.mDeviceClass = deviceClass;
    }

    public String toString() {
        return ("UsbAudioDevice: [card: " + this.mCard) + (", device: " + this.mDevice) + (", name: " + this.mDeviceName) + (", hasPlayback: " + this.mHasPlayback) + (", hasCapture: " + this.mHasCapture) + (", class: 0x" + Integer.toHexString(this.mDeviceClass) + "]");
    }

    /* access modifiers changed from: package-private */
    public String toShortString() {
        return "[card:" + this.mCard + " device:" + this.mDevice + " " + this.mDeviceName + "]";
    }

    /* access modifiers changed from: package-private */
    public String getDeviceName() {
        return this.mDeviceName;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceNameAndDescription(String deviceName, String deviceDescription) {
        this.mDeviceName = deviceName;
        this.mDeviceDescription = deviceDescription;
    }

    /* access modifiers changed from: package-private */
    public void setHeadsetStatus(boolean isInputHeadset, boolean isOutputHeadset) {
        this.mIsInputHeadset = isInputHeadset;
        this.mIsOutputHeadset = isOutputHeadset;
    }
}
