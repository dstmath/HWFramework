package com.huawei.server.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Slog;

public class BatteryPreheatNotification {
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Battery PreHeat Notification";
    private static final String NOTIFICATION_CHANNEL_ID = "BatteryPreHeatNotification";
    private static final String NOTIFICATION_CHANNEL_NAME = "BatteryPreHeatNotification Channel";
    private static final int NOTIFICATION_ID = 9999;
    private static final String TAG = "BatteryPreheatNotification";
    private Context mContext;
    private NotificationManager mNotificationManager;

    public BatteryPreheatNotification(Context context) {
        Slog.d(TAG, TAG);
        this.mContext = context;
        Object object = this.mContext.getSystemService("notification");
        if (object instanceof NotificationManager) {
            this.mNotificationManager = (NotificationManager) object;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, 4);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            this.mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void cancelNotification() {
        Slog.d(TAG, "cancelNotification");
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager == null) {
            Slog.e(TAG, "mNotificationManager is null");
        } else {
            notificationManager.cancel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_ID);
        }
    }

    public void notification() {
        Slog.d(TAG, "notification");
        if (this.mNotificationManager == null) {
            Slog.e(TAG, "mNotificationManager is null");
            return;
        }
        Notification.Builder notificationBuilder = getBaseNotificationBuilder(false);
        notificationBuilder.setContentTitle(this.mContext.getResources().getString(33686189));
        String contentTextString = this.mContext.getResources().getString(33686188);
        notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(contentTextString));
        notificationBuilder.setContentText(contentTextString);
        this.mNotificationManager.notify(NOTIFICATION_CHANNEL_ID, NOTIFICATION_ID, notificationBuilder.build());
    }

    private Notification.Builder getBaseNotificationBuilder(boolean onGoing) {
        Notification.Builder builder = new Notification.Builder(this.mContext, NOTIFICATION_CHANNEL_ID);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(33751969);
        builder.setOngoing(onGoing);
        builder.setAutoCancel(onGoing);
        builder.setDefaults(3);
        builder.setOnlyAlertOnce(true);
        return builder;
    }
}
