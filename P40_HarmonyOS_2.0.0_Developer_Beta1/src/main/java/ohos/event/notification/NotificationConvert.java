package ohos.event.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import java.util.Optional;

public class NotificationConvert {
    private static NotificationManager notificationManager;

    public static Optional<Notification> convertForegroundServiceNotification(NotificationRequest notificationRequest) {
        return NotificationTransformer.getInstance().transformForegroundServiceNotification(getNotificationManager(), notificationRequest);
    }

    private static NotificationManager getNotificationManager() {
        Context aospContext;
        if (notificationManager == null && (aospContext = NotificationTransformer.getInstance().getAospContext()) != null) {
            Object systemService = aospContext.getSystemService("notification");
            if (systemService instanceof NotificationManager) {
                notificationManager = (NotificationManager) systemService;
            }
        }
        return notificationManager;
    }

    private NotificationConvert() {
    }
}
