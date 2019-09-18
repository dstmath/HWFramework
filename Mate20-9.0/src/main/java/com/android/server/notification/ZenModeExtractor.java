package com.android.server.notification;

import android.content.Context;
import android.util.Log;
import android.util.Slog;

public class ZenModeExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final String TAG = "ZenModeExtractor";
    private ZenModeHelper mZenModeHelper;

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
        } else if (this.mZenModeHelper == null) {
            if (DBG) {
                Slog.d(TAG, "skipping - no zen info available");
            }
            return null;
        } else {
            record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
            if (record.isIntercepted()) {
                record.setSuppressedVisualEffects(this.mZenModeHelper.getNotificationPolicy().suppressedVisualEffects);
            } else {
                record.setSuppressedVisualEffects(0);
            }
            return null;
        }
    }

    public void setConfig(RankingConfig config) {
    }

    public void setZenHelper(ZenModeHelper helper) {
        this.mZenModeHelper = helper;
    }
}
