package com.android.server.notification;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Slog;

public class NotificationIntrusivenessExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    static final long HANG_TIME_MS = 10000;
    private static final String TAG = "IntrusivenessExtractor";

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
        if (DBG) {
            Slog.d(TAG, "Initializing  " + getClass().getSimpleName() + ".");
        }
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null) {
            if (DBG) {
                Slog.d(TAG, "skipping empty notification");
            }
            return null;
        }
        if (((long) record.getFreshnessMs(System.currentTimeMillis())) < 10000 && record.getImportance() >= 3) {
            if (!(record.getSound() == null || record.getSound() == Uri.EMPTY)) {
                record.setRecentlyIntrusive(true);
            }
            if (record.getVibration() != null) {
                record.setRecentlyIntrusive(true);
            }
            if (record.getNotification().fullScreenIntent != null) {
                record.setRecentlyIntrusive(true);
            }
        }
        return new RankingReconsideration(record.getKey(), 10000) {
            public void work() {
            }

            public void applyChangesLocked(NotificationRecord record) {
                if (System.currentTimeMillis() - record.getLastIntrusive() >= 10000) {
                    record.setRecentlyIntrusive(false);
                }
            }
        };
    }

    public void setConfig(RankingConfig config) {
    }
}
