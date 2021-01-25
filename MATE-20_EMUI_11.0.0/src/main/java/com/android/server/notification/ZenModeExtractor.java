package com.android.server.notification;

import android.content.Context;
import android.util.Log;
import android.util.Slog;

public class ZenModeExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final String TAG = "ZenModeExtractor";
    private ZenModeHelper mZenModeHelper;

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
        ZenModeHelper zenModeHelper = this.mZenModeHelper;
        if (zenModeHelper == null) {
            if (DBG) {
                Slog.d(TAG, "skipping - no zen info available");
            }
            return null;
        }
        record.setIntercepted(zenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            record.setSuppressedVisualEffects(this.mZenModeHelper.getConsolidatedNotificationPolicy().suppressedVisualEffects);
        } else {
            record.setSuppressedVisualEffects(0);
        }
        return null;
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setConfig(RankingConfig config) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setZenHelper(ZenModeHelper helper) {
        this.mZenModeHelper = helper;
    }
}
