package com.android.server.notification;

import android.content.Context;

public class CriticalNotificationExtractor implements NotificationSignalExtractor {
    static final int CRITICAL = 0;
    static final int CRITICAL_LOW = 1;
    private static final boolean DBG = false;
    static final int NORMAL = 2;
    private static final String TAG = "CriticalNotificationExt";
    private boolean mSupportsCriticalNotifications = false;

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void initialize(Context context, NotificationUsageStats usageStats) {
        this.mSupportsCriticalNotifications = supportsCriticalNotifications(context);
    }

    private boolean supportsCriticalNotifications(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.type.automotive", 0);
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public RankingReconsideration process(NotificationRecord record) {
        if (!this.mSupportsCriticalNotifications || record == null || record.getNotification() == null) {
            return null;
        }
        if (record.isCategory("car_emergency")) {
            record.setCriticality(0);
        } else if (record.isCategory("car_warning")) {
            record.setCriticality(1);
        } else {
            record.setCriticality(2);
        }
        return null;
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setConfig(RankingConfig config) {
    }

    @Override // com.android.server.notification.NotificationSignalExtractor
    public void setZenHelper(ZenModeHelper helper) {
    }
}
