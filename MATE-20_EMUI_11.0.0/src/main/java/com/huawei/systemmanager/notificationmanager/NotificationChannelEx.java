package com.huawei.systemmanager.notificationmanager;

import android.app.NotificationChannel;

public class NotificationChannelEx {
    public static boolean isBlockableSystem(NotificationChannel channel) {
        if (channel != null) {
            return channel.isBlockableSystem();
        }
        return false;
    }
}
