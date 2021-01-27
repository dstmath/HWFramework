package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;

public interface IHwNotificationManagerServiceEx {
    void adjustNotificationGroupIfNeeded(Notification notification, int i);

    String getHwOpPkg(StatusBarNotification statusBarNotification);

    String getPushSpecialRequestChannel(String str);

    String getPushSpecialRequestPkg(String str, String str2);

    String getPushSpecialRequestTag(String str);

    void init(Context context);

    boolean isBanNotification(String str, Notification notification);

    boolean isPushSpecialRequest(String str, String str2);

    boolean isSendNotificationDisable(int i, String str, Notification notification);
}
