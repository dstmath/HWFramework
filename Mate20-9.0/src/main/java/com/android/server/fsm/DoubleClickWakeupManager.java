package com.android.server.fsm;

import android.content.Context;
import android.os.PowerManagerInternal;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.LocalServices;

public final class DoubleClickWakeupManager extends WakeupManager {
    private static final String TAG = "Fsm_DoubleClickWakeupManager";
    private PowerManagerInternal mPowerManagerInternal;

    DoubleClickWakeupManager(Context context) {
        super(context);
    }

    public void wakeup() {
        Slog.d("Fsm_DoubleClickWakeupManager", "Wakeup in DoubleClickWakeupManager");
        if (this.mPowerManagerInternal == null) {
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }
        this.mPowerManagerInternal.powerWakeup(SystemClock.uptimeMillis(), this.mReason, this.mUid, this.mOpPackageName, this.mUid);
    }
}
