package android.media.update;

import android.app.Notification;
import android.content.Intent;
import android.media.MediaSession2;
import android.media.MediaSessionService2;
import android.os.IBinder;

public interface MediaSessionService2Provider {

    public interface MediaNotificationProvider {
        int getNotificationId_impl();

        Notification getNotification_impl();
    }

    MediaSession2 getSession_impl();

    IBinder onBind_impl(Intent intent);

    void onCreate_impl();

    MediaSessionService2.MediaNotification onUpdateNotification_impl();
}
