package com.huawei.android.hardware.usb;

import android.hardware.usb.UsbManager;

public class UsbManagerExt {
    public static final String USB_FUNCTION_MTP = "mtp";
    public static final String USB_FUNCTION_NONE = "none";
    public static final String USB_FUNCTION_PTP = "ptp";

    public static long usbFunctionsFromString(String functions) {
        return UsbManager.usbFunctionsFromString(functions);
    }
}
