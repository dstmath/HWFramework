package com.huawei.systemmanager.notificationmanager;

public class HwNotificationManagerEx {
    public static IHwNotificationManager getNotificationManager() {
        return HwNotificationManagerImpl.getInstance();
    }
}
