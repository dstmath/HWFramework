package com.android.server.connectivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.List;

public class HwNotificationTetheringDummy implements HwNotificationTethering {
    @Override // com.android.server.connectivity.HwNotificationTethering
    public void setTetheringNumber(boolean wifiTethered, boolean usbTethered, boolean bluetoothTethered) {
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public void setTetheringNumber(List<String> list) {
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public int getNotificationType(List<String> list) {
        return -1;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public int getNotificationIcon(int notificationType) {
        return 0;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public CharSequence getNotificationTitle(int notificationType) {
        return null;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public CharSequence getNotificationActionText(int notificationType) {
        return null;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public Intent getNotificationIntent(int notificationType) {
        return null;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public void showTetheredNotification(int notificationType, Notification notification, PendingIntent pi) {
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public boolean sendTetherNotification(Notification tetheredNotification, CharSequence title, CharSequence message, PendingIntent pi) {
        return false;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public void sendTetherNotification() {
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public void clearTetheredNotification() {
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public int getTetheredIcon(boolean usbTethered, boolean wifiTethered, boolean bluetoothTethered, boolean p2pTethered) {
        return 0;
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public void stopTethering() {
    }

    @Override // com.android.server.connectivity.HwNotificationTethering
    public void setWifiTetherd(boolean wifiTetherd) {
    }
}
