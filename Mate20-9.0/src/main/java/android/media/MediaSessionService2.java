package android.media;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.update.ApiLoader;
import android.media.update.MediaSessionService2Provider;
import android.os.IBinder;

public abstract class MediaSessionService2 extends Service {
    public static final String SERVICE_INTERFACE = "android.media.MediaSessionService2";
    public static final String SERVICE_META_DATA = "android.media.session";
    private final MediaSessionService2Provider mProvider = createProvider();

    public static class MediaNotification {
        private final MediaSessionService2Provider.MediaNotificationProvider mProvider;

        public MediaNotification(int notificationId, Notification notification) {
            this.mProvider = ApiLoader.getProvider().createMediaSessionService2MediaNotification(this, notificationId, notification);
        }

        public int getNotificationId() {
            return this.mProvider.getNotificationId_impl();
        }

        public Notification getNotification() {
            return this.mProvider.getNotification_impl();
        }
    }

    public abstract MediaSession2 onCreateSession(String str);

    /* access modifiers changed from: package-private */
    public MediaSessionService2Provider createProvider() {
        return ApiLoader.getProvider().createMediaSessionService2(this);
    }

    public void onCreate() {
        super.onCreate();
        this.mProvider.onCreate_impl();
    }

    public MediaNotification onUpdateNotification() {
        return this.mProvider.onUpdateNotification_impl();
    }

    public final MediaSession2 getSession() {
        return this.mProvider.getSession_impl();
    }

    public IBinder onBind(Intent intent) {
        return this.mProvider.onBind_impl(intent);
    }
}
