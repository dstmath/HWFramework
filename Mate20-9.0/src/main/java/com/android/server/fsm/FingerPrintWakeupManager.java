package com.android.server.fsm;

import android.content.Context;
import android.os.PowerManagerInternal;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.LocalServices;

public final class FingerPrintWakeupManager extends WakeupManager {
    private static final String TAG = "Fsm_FingerPrintWakeupManager";
    private boolean mFingerprintReady;
    private boolean mFoldScreenReady;
    private PowerManagerInternal mPowerManagerInternal;

    FingerPrintWakeupManager(Context context) {
        super(context);
    }

    public void setFoldScreenReady() {
        this.mFoldScreenReady = true;
    }

    public void setFingerprintReady() {
        this.mFingerprintReady = true;
    }

    public void wakeup() {
        Slog.d("Fsm_FingerPrintWakeupManager", "Wakeup in FingerPrintWakeupManager");
        if (this.mFoldScreenReady && this.mFingerprintReady) {
            if (this.mPowerManagerInternal == null) {
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            }
            this.mPowerManagerInternal.powerWakeup(SystemClock.uptimeMillis(), this.mReason, this.mUid, this.mOpPackageName, this.mUid);
            this.mFoldScreenReady = false;
            this.mFingerprintReady = false;
        }
    }
}
