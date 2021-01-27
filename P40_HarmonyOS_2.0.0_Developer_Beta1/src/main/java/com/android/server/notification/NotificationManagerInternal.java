package com.android.server.notification;

import android.app.Notification;
import android.app.NotificationChannel;

public interface NotificationManagerInternal {
    void enqueueNotification(String str, String str2, int i, int i2, String str3, int i3, Notification notification, int i4);

    NotificationChannel getNotificationChannel(String str, int i, String str2);

    void removeForegroundServiceFlagFromNotification(String str, int i, int i2);
}
