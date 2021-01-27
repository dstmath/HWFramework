package com.android.server.adb;

import android.content.Context;

public class AdbServiceBridge extends AdbService {
    private AdbServiceEx mAdbServiceEx;

    public AdbServiceBridge(Context context) {
        super(context);
    }

    public void setAdbServiceEx(AdbServiceEx adbServiceEx) {
        this.mAdbServiceEx = adbServiceEx;
    }

    public void systemReady() {
        AdbServiceBridge.super.systemReady();
        AdbServiceEx adbServiceEx = this.mAdbServiceEx;
        if (adbServiceEx != null) {
            adbServiceEx.systemReady();
        }
    }

    public void bootCompleted() {
        AdbServiceBridge.super.bootCompleted();
        AdbServiceEx adbServiceEx = this.mAdbServiceEx;
        if (adbServiceEx != null) {
            adbServiceEx.bootCompleted();
        }
    }

    /* access modifiers changed from: protected */
    public void setHdbEnabled(boolean isEnable) {
        AdbServiceEx adbServiceEx = this.mAdbServiceEx;
        if (adbServiceEx != null) {
            adbServiceEx.setHdbEnabled(isEnable);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwtiched(int newUserId) {
        AdbServiceEx adbServiceEx = this.mAdbServiceEx;
        if (adbServiceEx != null) {
            adbServiceEx.handleUserSwtiched(newUserId);
        }
    }

    /* access modifiers changed from: protected */
    public void onInitHandle() {
        AdbServiceEx adbServiceEx = this.mAdbServiceEx;
        if (adbServiceEx != null) {
            adbServiceEx.onInitHandle();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabled(boolean isEnable) {
        AdbServiceEx adbServiceEx = this.mAdbServiceEx;
        if (adbServiceEx != null) {
            return adbServiceEx.isAdbDisabled(isEnable);
        }
        return false;
    }
}
