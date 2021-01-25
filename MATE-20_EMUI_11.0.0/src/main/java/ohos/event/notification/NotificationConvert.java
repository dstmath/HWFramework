package ohos.event.notification;

import android.app.ActivityThread;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import java.util.Optional;

public class NotificationConvert {
    private static Context aospContext;
    private static NotificationManager notificationManager;

    public static Optional<Notification> convertForegroundServiceNotification(NotificationRequest notificationRequest) {
        return NotificationTransformer.getInstance().transformForegroundServiceNotification(getAospContext(), getNotificationManager(), notificationRequest);
    }

    private static Context getAospContext() {
        Application currentApplication;
        if (aospContext == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            aospContext = currentApplication.getApplicationContext();
        }
        return aospContext;
    }

    private static NotificationManager getNotificationManager() {
        Context aospContext2;
        if (notificationManager == null && (aospContext2 = getAospContext()) != null) {
            Object systemService = aospContext2.getSystemService("notification");
            if (systemService instanceof NotificationManager) {
                notificationManager = (NotificationManager) systemService;
            }
        }
        return notificationManager;
    }

    private NotificationConvert() {
    }
}
