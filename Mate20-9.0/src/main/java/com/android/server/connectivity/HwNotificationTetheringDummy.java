package com.android.server.connectivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.List;

public class HwNotificationTetheringDummy implements HwNotificationTethering {
    public void setTetheringNumber(boolean wifiTethered, boolean usbTethered, boolean bluetoothTethered) {
    }

    public void setTetheringNumber(List<String> list) {
    }

    public int getNotificationType(List<String> list) {
        return -1;
    }

    public int getNotificationIcon(int notificationType) {
        return 0;
    }

    public CharSequence getNotificationTitle(int notificationType) {
        return null;
    }

    public CharSequence getNotificationActionText(int notificationType) {
        return null;
    }

    public Intent getNotificationIntent(int notificationType) {
        return null;
    }

    public void showTetheredNotification(int notificationType, Notification notification, PendingIntent pi) {
    }

    public boolean sendTetherNotification(Notification tetheredNotification, CharSequence title, CharSequence message, PendingIntent pi) {
        return false;
    }

    public void sendTetherNotification() {
    }

    public void clearTetheredNotification() {
    }

    public int getTetheredIcon(boolean usbTethered, boolean wifiTethered, boolean bluetoothTethered, boolean p2pTethered) {
        return 0;
    }

    public void stopTethering() {
    }
}
