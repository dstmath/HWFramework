package com.android.server.usb;

import android.content.Context;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;

public abstract class AbsUsbDeviceManager {
    protected boolean getUsbHandlerConnected() {
        return false;
    }

    protected boolean sendHandlerEmptyMessage(int what) {
        return false;
    }

    protected Context getContext() {
        return null;
    }

    protected boolean containsFunctionOuter(String functions, String function) {
        return false;
    }

    protected void dueSimStatusCompletedMsg() {
    }

    protected boolean interceptSetEnabledFunctions(String functions) {
        return false;
    }

    protected void setEnabledFunctionsEx(String functions, boolean forceRestart) {
    }

    protected boolean isCmccUsbLimit() {
        return false;
    }

    protected String removeAdbFunction(String functions, String function) {
        return functions;
    }

    protected boolean setUsbConfigEx(String config) {
        return false;
    }

    protected boolean isAdbDisabled() {
        return false;
    }

    protected void usbWaterInNotification(boolean enable) {
    }

    public void setHdbEnabledEx(boolean enable) {
    }

    public void dump(FileDescriptor fd, IndentingPrintWriter writer, String[] args) {
    }
}
