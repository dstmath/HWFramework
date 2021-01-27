package com.huawei.android.app;

import android.app.INotificationManager;
import android.app.NotificationManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Slog;
import java.util.List;

public class INotificationManagerEx {
    private static final String TAG = "INotificationManagerEx";

    public static void cancelAllNotifications(List<String> packageList, int targetUid) throws RemoteException {
        INotificationManager service = NotificationManager.getService();
        if (service != null) {
            int userId = UserHandle.getUserId(targetUid);
            for (String pkgName : packageList) {
                service.cancelAllNotifications(pkgName, userId);
            }
        }
    }

    public static void cleanNotificationWithPid(List<String> packageList, int targetUid, int pid) throws RemoteException {
        INotificationManager service = NotificationManager.getService();
        if (service != null) {
            StatusBarNotification[] notifications = service.getActiveNotifications("android");
            int userId = UserHandle.getUserId(targetUid);
            if (notifications != null) {
                for (StatusBarNotification notification : notifications) {
                    if (notification.getInitialPid() == pid) {
                        for (String packageName : packageList) {
                            service.cancelNotificationWithTag(packageName, notification.getTag(), notification.getId(), userId);
                        }
                    }
                }
            }
        }
    }

    public static boolean hasNotification(int pid) {
        INotificationManager service = NotificationManager.getService();
        if (service == null) {
            return false;
        }
        try {
            StatusBarNotification[] notifications = service.getActiveNotifications("android");
            if (notifications == null) {
                return false;
            }
            for (StatusBarNotification notification : notifications) {
                if (notification != null && notification.getInitialPid() == pid) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to talk to notification manager. Woe!");
        }
    }
}
