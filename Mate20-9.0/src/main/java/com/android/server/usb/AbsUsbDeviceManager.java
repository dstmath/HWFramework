package com.android.server.usb;

import android.content.Context;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;

public abstract class AbsUsbDeviceManager {
    /* access modifiers changed from: protected */
    public boolean getUsbHandlerConnected() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean sendHandlerEmptyMessage(int what) {
        return false;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean containsFunctionOuter(String functions, String function) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void dueSimStatusCompletedMsg() {
    }

    /* access modifiers changed from: protected */
    public boolean interceptSetEnabledFunctions(String functions) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setEnabledFunctionsEx(String functions, boolean forceRestart) {
    }

    /* access modifiers changed from: protected */
    public boolean isCmccUsbLimit() {
        return false;
    }

    /* access modifiers changed from: protected */
    public String removeAdbFunction(String functions, String function) {
        return functions;
    }

    /* access modifiers changed from: protected */
    public boolean setUsbConfigEx(String config) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabled() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendUSBLiquidBroadcast(Context context, String msg) {
    }

    /* access modifiers changed from: protected */
    public void usbWaterInNotification(boolean enable) {
    }

    public void setHdbEnabledEx(boolean enable) {
    }

    public void dump(FileDescriptor fd, IndentingPrintWriter writer, String[] args) {
    }
}
