package com.android.server.notification;

public class AlertRateLimiter {
    static final long ALLOWED_ALERT_INTERVAL = 1000;
    private long mLastNotificationMillis = 0;

    /* access modifiers changed from: package-private */
    public boolean shouldRateLimitAlert(long now) {
        long millisSinceLast = now - this.mLastNotificationMillis;
        if (millisSinceLast < 0 || millisSinceLast < 1000) {
            return true;
        }
        this.mLastNotificationMillis = now;
        return false;
    }
}
