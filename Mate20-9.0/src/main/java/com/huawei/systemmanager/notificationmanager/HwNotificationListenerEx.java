package com.huawei.systemmanager.notificationmanager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;

public class HwNotificationListenerEx {
    public void registerAsSystemService(NotificationListenerService service, Context context, ComponentName name, int currentUser) throws RemoteException {
        service.registerAsSystemService(context, name, currentUser);
    }

    public void unregisterAsSystemService(NotificationListenerService service) throws RemoteException {
        service.unregisterAsSystemService();
    }

    public static int getDefaultInitialVisibility() {
        return -1000;
    }

    public static int getRankingVisibility(NotificationListenerService.Ranking ranking) {
        return ranking.getVisibilityOverride();
    }
}
