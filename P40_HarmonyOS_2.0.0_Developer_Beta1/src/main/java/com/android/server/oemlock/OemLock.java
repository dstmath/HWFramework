package com.android.server.oemlock;

/* access modifiers changed from: package-private */
public abstract class OemLock {
    /* access modifiers changed from: package-private */
    public abstract String getLockName();

    /* access modifiers changed from: package-private */
    public abstract boolean isOemUnlockAllowedByCarrier();

    /* access modifiers changed from: package-private */
    public abstract boolean isOemUnlockAllowedByDevice();

    /* access modifiers changed from: package-private */
    public abstract void setOemUnlockAllowedByCarrier(boolean z, byte[] bArr);

    /* access modifiers changed from: package-private */
    public abstract void setOemUnlockAllowedByDevice(boolean z);

    OemLock() {
    }
}
