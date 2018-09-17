package com.huawei.android.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;

public class NotificationEx {

    public static class Builder {
        public static void setAppName(android.app.Notification.Builder builder, CharSequence appName) {
            builder.setAppName(appName);
        }
    }

    public static void setLatestEventInfo(Notification notification, Context context, CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent) {
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    }
}
