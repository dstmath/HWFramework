package com.android.server.usb;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.usb.UsbManager;

public class UsbDeviceManagerEx {
    protected static final int MSG_ENABLE_ALLOWCHARGINGADB = 104;
    protected static final int MSG_SIM_COMPLETED = 102;
    private UsbDeviceManagerBridge mBridge = null;

    public UsbDeviceManagerEx(Context context, UsbAlsaManagerEx alsaManagerEx, UsbSettingsManagerEx settingsManagerEx) {
        this.mBridge = new UsbDeviceManagerBridge(context, alsaManagerEx.getUsbAlsaManager(), settingsManagerEx.getUsbSettingsManager());
        this.mBridge.setUsbDeviceManagerEx(this);
    }

    public UsbDeviceManager getUsbDeviceManager() {
        return this.mBridge;
    }

    public void bootCompleted() {
    }

    /* access modifiers changed from: protected */
    public void handleSimStatusCompleted() {
    }

    /* access modifiers changed from: protected */
    public boolean interceptSetEnabledFunctions(String functions) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isCmccUsbLimit() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabled() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onInitHandler() {
    }

    /* access modifiers changed from: protected */
    public boolean isRepairMode() {
        return false;
    }

    /* access modifiers changed from: protected */
    public String applyHdbFunction(String functions) {
        return functions;
    }

    /* access modifiers changed from: protected */
    public String applyUserRestrictions(String functions) {
        return functions;
    }

    public ContentResolver getContentResolver() {
        return this.mBridge.mContentResolver;
    }

    public Context getContext() {
        return this.mBridge.mContext;
    }

    /* access modifiers changed from: protected */
    public void sendHandlerMessage(int what, boolean isArg) {
        if (this.mBridge.mHandler != null) {
            this.mBridge.mHandler.sendMessage(what, isArg);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getUsbHandlerConnected() {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.mConnected;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean sendHandlerEmptyMessage(int what) {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.sendEmptyMessage(what);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean containsFunctionOuter(String functions, String function) {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.containsFunction(functions, function);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String addFunctionOuter(String functions, String function) {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.addFunction(functions, function);
        }
        return functions;
    }

    /* access modifiers changed from: protected */
    public String removeFunctionOuter(String functions, String function) {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.removeFunction(functions, function);
        }
        return functions;
    }

    /* access modifiers changed from: protected */
    public void setEnabledFunctionsEx(String functions, boolean isRestart) {
        if (this.mBridge.mHandler != null) {
            this.mBridge.mHandler.setEnabledFunctions(UsbManager.usbFunctionsFromString(functions), isRestart, false);
        }
    }

    /* access modifiers changed from: protected */
    public boolean setUsbConfigEx(String config) {
        if (this.mBridge.mHandler == null) {
            return false;
        }
        this.mBridge.mHandler.setUsbConfig(config);
        return this.mBridge.mHandler.waitForState(config);
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabledByDevicePolicy() {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.isAdbDisabledByDevicePolicy();
        }
        return false;
    }
}
