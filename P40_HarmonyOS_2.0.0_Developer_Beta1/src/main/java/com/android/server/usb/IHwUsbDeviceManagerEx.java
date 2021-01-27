package com.android.server.usb;

public interface IHwUsbDeviceManagerEx {
    void notifyHiCarInfo(byte[] bArr, boolean z);

    void notifyNearbyInfo(byte[] bArr, boolean z);
}
