package android.app;

import android.graphics.drawable.Icon;
import android.widget.RemoteViews;

public interface IHwNotificationEx {
    boolean isPureColorIcon(Notification notification, Icon icon, boolean z);

    void preProcessLineView(RemoteViews remoteViews, Notification notification);

    void preProcessRemoteView(String str, RemoteViews remoteViews, Notification notification);
}
