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
    public String mDeviceDescription = "";
    public String mDeviceName = "";
    public final boolean mHasCapture;
    public final boolean mHasPlayback;

    public UsbAudioDevice(int card, int device, boolean hasPlayback, boolean hasCapture, int deviceClass) {
        this.mCard = card;
        this.mDevice = device;
        this.mHasPlayback = hasPlayback;
        this.mHasCapture = hasCapture;
        this.mDeviceClass = deviceClass;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UsbAudioDevice: [card: ").append(this.mCard);
        sb.append(", device: ").append(this.mDevice);
        sb.append(", name: ").append(this.mDeviceName);
        sb.append(", hasPlayback: ").append(this.mHasPlayback);
        sb.append(", hasCapture: ").append(this.mHasCapture);
        sb.append(", class: 0x").append(Integer.toHexString(this.mDeviceClass)).append("]");
        return sb.toString();
    }

    public String toShortString() {
        return "[card:" + this.mCard + " device:" + this.mDevice + " " + this.mDeviceName + "]";
    }
}
