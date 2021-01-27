package com.huawei.android.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.UserHandle;

public class NotificationManagerExt {
    public static void notifyAsUser(NotificationManager notificationManager, String tag, int id, Notification notification, UserHandle user) {
        notificationManager.notifyAsUser(tag, id, notification, user);
    }

    public static void cancelAsUser(NotificationManager notificationManager, String tag, int id, UserHandle user) {
        notificationManager.cancelAsUser(tag, id, user);
    }
}
