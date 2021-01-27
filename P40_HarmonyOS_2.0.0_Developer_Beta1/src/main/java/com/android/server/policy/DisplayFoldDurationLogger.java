package com.android.server.policy;

import android.metrics.LogMaker;
import android.os.SystemClock;
import com.android.internal.logging.MetricsLogger;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* access modifiers changed from: package-private */
public class DisplayFoldDurationLogger {
    private static final int LOG_SUBTYPE_DURATION_MASK = Integer.MIN_VALUE;
    private static final int LOG_SUBTYPE_FOLDED = 1;
    private static final int LOG_SUBTYPE_UNFOLDED = 0;
    static final int SCREEN_STATE_OFF = 0;
    static final int SCREEN_STATE_ON_FOLDED = 2;
    static final int SCREEN_STATE_ON_UNFOLDED = 1;
    static final int SCREEN_STATE_UNKNOWN = -1;
    private Long mLastChanged = null;
    private final MetricsLogger mLogger = new MetricsLogger();
    private int mScreenState = -1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScreenState {
    }

    DisplayFoldDurationLogger() {
    }

    /* access modifiers changed from: package-private */
    public void onFinishedWakingUp(Boolean folded) {
        if (folded == null) {
            this.mScreenState = -1;
        } else if (folded.booleanValue()) {
            this.mScreenState = 2;
        } else {
            this.mScreenState = 1;
        }
        this.mLastChanged = Long.valueOf(SystemClock.uptimeMillis());
    }

    /* access modifiers changed from: package-private */
    public void onFinishedGoingToSleep() {
        log();
        this.mScreenState = 0;
        this.mLastChanged = null;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceFolded(boolean folded) {
        if (isOn()) {
            log();
            this.mScreenState = folded ? 2 : 1;
            this.mLastChanged = Long.valueOf(SystemClock.uptimeMillis());
        }
    }

    /* access modifiers changed from: package-private */
    public void logFocusedAppWithFoldState(boolean folded, String packageName) {
        this.mLogger.write(new LogMaker(1594).setType(4).setSubtype(folded ? 1 : 0).setPackageName(packageName));
    }

    private void log() {
        int subtype;
        if (this.mLastChanged != null) {
            int i = this.mScreenState;
            if (i == 1) {
                subtype = Integer.MIN_VALUE;
            } else if (i == 2) {
                subtype = -2147483647;
            } else {
                return;
            }
            this.mLogger.write(new LogMaker(1594).setType(4).setSubtype(subtype).setLatency(SystemClock.uptimeMillis() - this.mLastChanged.longValue()));
        }
    }

    private boolean isOn() {
        int i = this.mScreenState;
        return i == 1 || i == 2;
    }
}
