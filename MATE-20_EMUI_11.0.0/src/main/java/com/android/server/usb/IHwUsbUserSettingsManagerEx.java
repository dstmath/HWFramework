package com.android.server.usb;

import android.app.PendingIntent;
import android.hardware.usb.UsbDevice;

public interface IHwUsbUserSettingsManagerEx {
    boolean removeUsbPermissionDialog(UsbDevice usbDevice, String str, PendingIntent pendingIntent, int i);
}
