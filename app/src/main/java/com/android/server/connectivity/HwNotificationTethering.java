package com.android.server.connectivity;

import android.app.Notification;
import android.app.PendingIntent;

public interface HwNotificationTethering {
    void clearTetheredNotification();

    int getTetheredIcon(boolean z, boolean z2, boolean z3, boolean z4);

    void sendTetherNotification();

    boolean sendTetherNotification(Notification notification, CharSequence charSequence, CharSequence charSequence2, PendingIntent pendingIntent);

    void setTetheringNumber(boolean z, boolean z2, boolean z3);

    void stopTethering();
}
