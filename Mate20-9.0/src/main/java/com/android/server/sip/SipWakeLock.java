package com.android.server.sip;

import android.os.PowerManager;
import android.telephony.Rlog;
import java.util.HashSet;

class SipWakeLock {
    private static final boolean DBG = false;
    private static final String TAG = "SipWakeLock";
    private HashSet<Object> mHolders = new HashSet<>();
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mTimerWakeLock;
    private PowerManager.WakeLock mWakeLock;

    SipWakeLock(PowerManager powerManager) {
        this.mPowerManager = powerManager;
    }

    /* access modifiers changed from: package-private */
    public synchronized void reset() {
        this.mHolders.clear();
        release(null);
    }

    /* access modifiers changed from: package-private */
    public synchronized void acquire(long timeout) {
        if (this.mTimerWakeLock == null) {
            this.mTimerWakeLock = this.mPowerManager.newWakeLock(1, "SipWakeLock.timer");
            this.mTimerWakeLock.setReferenceCounted(true);
        }
        this.mTimerWakeLock.acquire(timeout);
    }

    /* access modifiers changed from: package-private */
    public synchronized void acquire(Object holder) {
        this.mHolders.add(holder);
        if (this.mWakeLock == null) {
            this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
        }
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void release(Object holder) {
        this.mHolders.remove(holder);
        if (this.mWakeLock != null && this.mHolders.isEmpty() && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }
}
