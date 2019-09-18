package com.android.server.notification;

import android.content.Context;

public class VisibilityExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "VisibilityExtractor";
    private RankingConfig mConfig;

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    public RankingReconsideration process(NotificationRecord record) {
        if (record == null || record.getNotification() == null || this.mConfig == null) {
            return null;
        }
        record.setPackageVisibilityOverride(record.getChannel().getLockscreenVisibility());
        return null;
    }

    public void setConfig(RankingConfig config) {
        this.mConfig = config;
    }

    public void setZenHelper(ZenModeHelper helper) {
    }
}
