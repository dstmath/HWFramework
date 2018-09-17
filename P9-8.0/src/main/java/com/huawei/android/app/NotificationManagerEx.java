package com.huawei.android.app;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.os.RemoteException;
import android.os.UserHandle;

public class NotificationManagerEx {
    public static void enqueueToast(String packageName, TransientNotificationEx transientNotification, int duration) throws RemoteException {
        NotificationManager.getService().enqueueToast(packageName, transientNotification.getStub(), duration);
    }

    public static void cancelToast(String packageName, TransientNotificationEx transientNotification) throws RemoteException {
        NotificationManager.getService().cancelToast(packageName, transientNotification.getStub());
    }

    public static Builder setAppName(Builder builder, CharSequence appName) {
        builder.setAppName(appName);
        return builder;
    }

    public static Builder setShowActionIcon(Builder builder, boolean showActionIcon) {
        builder.setShowActionIcon(showActionIcon);
        return builder;
    }

    public static void cancelAsUser(NotificationManager notificationManager, String tag, int id, UserHandle user) {
        notificationManager.cancelAsUser(tag, id, user);
    }

    public static void notifyAsUser(NotificationManager notificationManager, String tag, int id, Notification notification, UserHandle user) {
        notificationManager.notifyAsUser(tag, id, notification, user);
    }
}
