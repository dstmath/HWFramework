package com.android.server.notification;

import android.content.Context;

public class BadgeExtractor implements NotificationSignalExtractor {
    private static final boolean DBG = false;
    private static final String TAG = "BadgeExtractor";
    private RankingConfig mConfig;

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void initialize(Context ctx, NotificationUsageStats usageStats) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public RankingReconsideration process(NotificationRecord record) {
        RankingConfig rankingConfig;
        if (record == null || record.getNotification() == null || (rankingConfig = this.mConfig) == null) {
            return null;
        }
        boolean userWantsBadges = rankingConfig.badgingEnabled(record.sbn.getUser());
        boolean appCanShowBadge = this.mConfig.canShowBadge(record.sbn.getPackageName(), record.sbn.getUid());
        if (!userWantsBadges || !appCanShowBadge) {
            record.setShowBadge(false);
        } else if (record.getChannel() != null) {
            record.setShowBadge(record.getChannel().canShowBadge() && appCanShowBadge);
        } else {
            record.setShowBadge(appCanShowBadge);
        }
        if (record.isIntercepted() && (record.getSuppressedVisualEffects() & 64) != 0) {
            record.setShowBadge(false);
        }
        return null;
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setConfig(RankingConfig config) {
        this.mConfig = config;
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setZenHelper(ZenModeHelper helper) {
    }
}
