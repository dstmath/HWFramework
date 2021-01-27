package com.android.server.usb;

public class UsbSettingsManagerEx {
    private UsbSettingsManager mUsbSettingsManager = null;

    public void setUsbSettingsManager(UsbSettingsManager usbSettingsManager) {
        this.mUsbSettingsManager = usbSettingsManager;
    }

    public UsbSettingsManager getUsbSettingsManager() {
        return this.mUsbSettingsManager;
    }
}
