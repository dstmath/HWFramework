package com.android.server.biometrics;

import android.content.Context;
import android.util.Slog;
import android.util.StatsLog;

public abstract class LoggableMonitor {
    public static final boolean DEBUG = false;
    public static final String TAG = "BiometricStats";
    private long mFirstAcquireTimeMs;

    /* access modifiers changed from: protected */
    public abstract int statsAction();

    /* access modifiers changed from: protected */
    public abstract int statsModality();

    /* access modifiers changed from: protected */
    public boolean isCryptoOperation() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int statsClient() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public final void logOnAcquired(Context context, int acquiredInfo, int vendorCode, int targetUserId) {
        if (statsModality() == 4) {
            if (acquiredInfo == 20) {
                this.mFirstAcquireTimeMs = System.currentTimeMillis();
            }
        } else if (acquiredInfo == 0 && this.mFirstAcquireTimeMs == 0) {
            this.mFirstAcquireTimeMs = System.currentTimeMillis();
        }
        StatsLog.write(87, statsModality(), targetUserId, isCryptoOperation(), statsAction(), statsClient(), acquiredInfo, 0, Utils.isDebugEnabled(context, targetUserId));
    }

    /* access modifiers changed from: protected */
    public final void logOnError(Context context, int error, int vendorCode, int targetUserId) {
        StatsLog.write(89, statsModality(), targetUserId, isCryptoOperation(), statsAction(), statsClient(), error, vendorCode, Utils.isDebugEnabled(context, targetUserId));
    }

    /* access modifiers changed from: protected */
    public final void logOnAuthenticated(Context context, boolean authenticated, boolean requireConfirmation, int targetUserId, boolean isBiometricPrompt) {
        int authState;
        long latency;
        if (!authenticated) {
            authState = 1;
        } else if (!isBiometricPrompt || !requireConfirmation) {
            authState = 3;
        } else {
            authState = 2;
        }
        if (this.mFirstAcquireTimeMs != 0) {
            latency = System.currentTimeMillis() - this.mFirstAcquireTimeMs;
        } else {
            latency = -1;
        }
        Slog.v(TAG, "Authentication latency: " + latency);
        StatsLog.write(88, statsModality(), targetUserId, isCryptoOperation(), statsClient(), requireConfirmation, authState, latency, Utils.isDebugEnabled(context, targetUserId));
    }

    /* access modifiers changed from: protected */
    public final void logOnEnrolled(int targetUserId, long latency, boolean enrollSuccessful) {
        Slog.v(TAG, "Enroll latency: " + latency);
        StatsLog.write(184, statsModality(), targetUserId, latency, enrollSuccessful);
    }
}
