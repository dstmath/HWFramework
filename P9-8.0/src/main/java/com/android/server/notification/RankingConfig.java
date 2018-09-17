package com.android.server.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.pm.ParceledListSlice;
import android.os.UserHandle;
import java.util.Collection;

public interface RankingConfig {
    boolean badgingEnabled(UserHandle userHandle);

    boolean canShowBadge(String str, int i);

    void createNotificationChannel(String str, int i, NotificationChannel notificationChannel, boolean z);

    void createNotificationChannelGroup(String str, int i, NotificationChannelGroup notificationChannelGroup, boolean z);

    void deleteNotificationChannel(String str, int i, String str2);

    int getImportance(String str, int i);

    NotificationChannel getNotificationChannel(String str, int i, String str2, boolean z);

    ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String str, int i, boolean z);

    Collection<NotificationChannelGroup> getNotificationChannelGroups(String str, int i);

    ParceledListSlice<NotificationChannel> getNotificationChannels(String str, int i, boolean z);

    void permanentlyDeleteNotificationChannel(String str, int i, String str2);

    void permanentlyDeleteNotificationChannels(String str, int i);

    void setImportance(String str, int i, int i2);

    void setShowBadge(String str, int i, boolean z);

    void updateNotificationChannel(String str, int i, NotificationChannel notificationChannel, boolean z);
}
