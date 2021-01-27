package com.android.server.usb;

public abstract class AbsUsbDeviceManager {
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
}
