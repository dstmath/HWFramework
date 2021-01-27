package com.android.server.notification;

import android.content.Context;

public class NotificationAdjustmentExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "AdjustmentExtractor";

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null) {
            return null;
        }
        record.applyAdjustments();
        return null;
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setConfig(RankingConfig config) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setZenHelper(ZenModeHelper helper) {
    }
}
