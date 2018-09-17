package com.android.server.notification;

import android.content.Context;

public class VisibilityExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "VisibilityExtractor";
    private RankingConfig mConfig;

    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
}
