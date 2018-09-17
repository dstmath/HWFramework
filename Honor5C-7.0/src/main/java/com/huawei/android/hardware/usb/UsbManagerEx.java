package com.huawei.android.hardware.usb;

import huawei.android.hardware.usb.HwUsbManagerEx;

public class UsbManagerEx {
    public static void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        HwUsbManagerEx.getInstance().allowUsbHDB(alwaysAllow, publicKey);
    }

    public static void denyUsbHDB() {
        HwUsbManagerEx.getInstance().denyUsbHDB();
    }

    public static void clearUsbHDBKeys() {
        HwUsbManagerEx.getInstance().clearUsbHDBKeys();
    }
}
