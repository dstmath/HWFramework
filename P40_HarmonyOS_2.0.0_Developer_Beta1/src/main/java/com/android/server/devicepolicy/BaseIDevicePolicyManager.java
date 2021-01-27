package com.android.server.devicepolicy;

import android.app.admin.IDevicePolicyManager;

/* access modifiers changed from: package-private */
public abstract class BaseIDevicePolicyManager extends IDevicePolicyManager.Stub {
    /* access modifiers changed from: package-private */
    public abstract void handleStartUser(int i);

    /* access modifiers changed from: package-private */
    public abstract void handleStopUser(int i);

    /* access modifiers changed from: package-private */
    public abstract void handleUnlockUser(int i);

    /* access modifiers changed from: package-private */
    public abstract void systemReady(int i);

    BaseIDevicePolicyManager() {
    }

    public void clearSystemUpdatePolicyFreezePeriodRecord() {
    }
}
