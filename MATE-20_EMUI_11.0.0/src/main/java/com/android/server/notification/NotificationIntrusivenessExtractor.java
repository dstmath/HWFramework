package com.android.server.notification;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;

public class NotificationIntrusivenessExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    @VisibleForTesting
    static final long HANG_TIME_MS = 10000;
    private static final String TAG = "IntrusivenessExtractor";

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void initialize(Context ctx, NotificationUsageStats usageStats) {
        if (DBG) {
            Slog.d(TAG, "Initializing  " + getClass().getSimpleName() + ".");
        }
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
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
        if (!record.isRecentlyIntrusive()) {
            return null;
        }
        return new RankingReconsideration(record.getKey(), 10000) {
            /* class com.android.server.notification.NotificationIntrusivenessExtractor.AnonymousClass1 */

            @Override // com.android.server.notification.RankingReconsideration
            public void work() {
            }

            @Override // com.android.server.notification.RankingReconsideration
            public void applyChangesLocked(NotificationRecord record) {
                if (System.currentTimeMillis() - record.getLastIntrusive() >= 10000) {
                    record.setRecentlyIntrusive(false);
                }
            }
        };
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setConfig(RankingConfig config) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setZenHelper(ZenModeHelper helper) {
    }
}
