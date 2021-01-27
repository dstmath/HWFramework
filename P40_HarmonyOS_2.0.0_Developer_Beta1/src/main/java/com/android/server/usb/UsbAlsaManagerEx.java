package com.android.server.usb;

public class UsbAlsaManagerEx {
    private UsbAlsaManager mUsbAlsaManager = null;

    public void setUsbAlsaManager(UsbAlsaManager usbAlsaManager) {
        this.mUsbAlsaManager = usbAlsaManager;
    }

    public UsbAlsaManager getUsbAlsaManager() {
        return this.mUsbAlsaManager;
    }
}
