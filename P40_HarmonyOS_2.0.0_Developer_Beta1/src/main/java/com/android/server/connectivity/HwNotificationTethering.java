package com.android.server.connectivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.List;

public interface HwNotificationTethering {
    void clearTetheredNotification();

    CharSequence getNotificationActionText(int i);

    int getNotificationIcon(int i);

    Intent getNotificationIntent(int i);

    CharSequence getNotificationTitle(int i);

    int getNotificationType(List<String> list);

    int getTetheredIcon(boolean z, boolean z2, boolean z3, boolean z4);

    void sendTetherNotification();

    boolean sendTetherNotification(Notification notification, CharSequence charSequence, CharSequence charSequence2, PendingIntent pendingIntent);

    void setTetheringNumber(List<String> list);

    void setTetheringNumber(boolean z, boolean z2, boolean z3);

    void setWifiTetherd(boolean z);

    void showTetheredNotification(int i, Notification notification, PendingIntent pendingIntent);

    void stopTethering();
}
