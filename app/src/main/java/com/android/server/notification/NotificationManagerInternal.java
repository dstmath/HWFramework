package com.android.server.notification;

import android.app.Notification;

public interface NotificationManagerInternal {
    void enqueueNotification(String str, String str2, int i, int i2, String str3, int i3, Notification notification, int[] iArr, int i4);

    void removeForegroundServiceFlagFromNotification(String str, int i, int i2);
}
