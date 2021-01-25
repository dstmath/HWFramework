package com.android.server.usb;

import android.content.Context;

public class UsbDeviceManagerBridge extends UsbDeviceManager {
    private UsbDeviceManagerEx mUsbDeviceManagerEx;

    public UsbDeviceManagerBridge(Context context, UsbAlsaManager alsaManager, UsbSettingsManager settingsManager) {
        super(context, alsaManager, settingsManager);
    }

    public void setUsbDeviceManagerEx(UsbDeviceManagerEx usbDeviceManagerEx) {
        this.mUsbDeviceManagerEx = usbDeviceManagerEx;
    }

    public void bootCompleted() {
        UsbDeviceManagerBridge.super.bootCompleted();
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            usbDeviceManagerEx.bootCompleted();
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimStatusCompleted() {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            usbDeviceManagerEx.handleSimStatusCompleted();
        }
    }

    /* access modifiers changed from: protected */
    public boolean interceptSetEnabledFunctions(String functions) {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            return usbDeviceManagerEx.interceptSetEnabledFunctions(functions);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isCmccUsbLimit() {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            return usbDeviceManagerEx.isCmccUsbLimit();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabled() {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            return usbDeviceManagerEx.isAdbDisabled();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onInitHandler() {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            usbDeviceManagerEx.onInitHandler();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRepairMode() {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            return usbDeviceManagerEx.isRepairMode();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String applyHdbFunction(String functions) {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            return usbDeviceManagerEx.applyHdbFunction(functions);
        }
        return functions;
    }

    /* access modifiers changed from: protected */
    public String applyUserRestrictions(String functions) {
        UsbDeviceManagerEx usbDeviceManagerEx = this.mUsbDeviceManagerEx;
        if (usbDeviceManagerEx != null) {
            return usbDeviceManagerEx.applyUserRestrictions(functions);
        }
        return functions;
    }
}
