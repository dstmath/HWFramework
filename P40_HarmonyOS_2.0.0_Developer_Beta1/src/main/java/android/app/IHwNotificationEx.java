package android.app;

import android.app.Notification;
import android.graphics.drawable.Icon;
import android.widget.RemoteViews;

public interface IHwNotificationEx {
    boolean isPureColorIcon(Notification notification, Icon icon, boolean z);

    void preActionButton(Notification.Action action, RemoteViews remoteViews, Notification notification);

    void preProcessLineView(RemoteViews remoteViews, Notification notification);

    void preProcessRemoteView(String str, RemoteViews remoteViews, Notification notification);
}
