package com.huawei.systemmanager.notificationmanager;

import android.app.NotificationChannel;
import android.os.Bundle;
import android.os.RemoteException;
import java.util.List;

public interface IHwNotificationManager {
    boolean areNotificationsEnabledForPackage(String str, int i) throws RemoteException;

    boolean canShowBadge(String str, int i) throws RemoteException;

    void cancelClearableNotifications(String str) throws RemoteException;

    int getClearableNotificationsNum(String str) throws RemoteException;

    NotificationChannel getNotificationChannelForPackage(String str, int i, String str2, boolean z) throws RemoteException;

    List<NotificationChannel> getNotificationChannelsForPackage(String str, int i, boolean z) throws RemoteException;

    boolean matchesCallFilter(Bundle bundle) throws RemoteException;

    void setNotificationsEnabledForPackage(String str, int i, boolean z) throws RemoteException;

    void setShowBadge(String str, int i, boolean z) throws RemoteException;

    void updateNotificationChannelForPackage(String str, int i, NotificationChannel notificationChannel) throws RemoteException;
}
