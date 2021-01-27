package com.android.server.adb;

import android.debug.IAdbManager;

public abstract class AbsAdbService extends IAdbManager.Stub {
    /* access modifiers changed from: protected */
    public boolean isAdbDisabled(boolean enable) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onInitHandle() {
    }

    /* access modifiers changed from: protected */
    public void setHdbEnabled(boolean enable) {
    }

    /* access modifiers changed from: protected */
    public void handleUserSwtiched(int newUserId) {
    }
}
