package com.android.server.notification;

import android.content.Context;

public class PriorityExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "PriorityExtractor";
    private RankingConfig mConfig;

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null || this.mConfig == null) {
            return null;
        }
        record.setPackagePriority(record.getChannel().canBypassDnd() ? 2 : 0);
        return null;
    }

    public void setConfig(RankingConfig config) {
        this.mConfig = config;
    }

    public void setZenHelper(ZenModeHelper helper) {
    }
}
