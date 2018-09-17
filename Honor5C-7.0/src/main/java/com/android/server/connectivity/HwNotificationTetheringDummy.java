package com.android.server.connectivity;

import android.app.Notification;
import android.app.PendingIntent;

public class HwNotificationTetheringDummy implements HwNotificationTethering {
    public void setTetheringNumber(boolean wifiTethered, boolean usbTethered, boolean bluetoothTethered) {
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
