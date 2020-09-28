package com.huawei.systemmanager.notificationmanager;

import android.app.NotificationChannel;
import android.os.Bundle;
import android.os.RemoteException;
import java.util.List;

public interface IHwNotificationManager {
    public static final int SILENT_NOTIFICATION_FEATURE_DISABLE = 0;
    public static final int SILENT_NOTIFICATION_FEATURE_ENABLE = 1;
    public static final int SILENT_NOTIFICATION_FEATURE_STATUS = 1;

    boolean areBubblesAllowedForPackage(String str, int i) throws RemoteException;

    boolean areNotificationsEnabledForPackage(String str, int i) throws RemoteException;

    boolean canShowBadge(String str, int i) throws RemoteException;

    void cancelClearableNotifications(String str) throws RemoteException;

    int getClearableNotificationsNum(String str) throws RemoteException;

    NotificationChannel getNotificationChannelForPackage(String str, int i, String str2, boolean z) throws RemoteException;

    List<NotificationChannel> getNotificationChannelsForPackage(String str, int i, boolean z) throws RemoteException;

    boolean matchesCallFilter(Bundle bundle) throws RemoteException;

    void setBubblesAllowed(String str, int i, boolean z) throws RemoteException;

    void setNotificationsEnabledForPackage(String str, int i, boolean z) throws RemoteException;

    void setShowBadge(String str, int i, boolean z) throws RemoteException;

    void updateNotificationChannelForPackage(String str, int i, NotificationChannel notificationChannel) throws RemoteException;
}
