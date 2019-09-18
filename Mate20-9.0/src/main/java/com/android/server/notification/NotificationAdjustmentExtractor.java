package com.android.server.notification;

import android.content.Context;

public class NotificationAdjustmentExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "AdjustmentExtractor";

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null) {
            return null;
        }
        record.applyAdjustments();
        return null;
    }

    public void setConfig(RankingConfig config) {
    }

    public void setZenHelper(ZenModeHelper helper) {
    }
}
