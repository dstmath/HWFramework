package com.huawei.android.hardware.usb;

import com.huawei.annotation.HwSystemApi;

public class HwUsbManagerAdapter {
    @HwSystemApi
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    @HwSystemApi
    public static final String USB_CONNECTED = "connected";

    private HwUsbManagerAdapter() {
    }
}
