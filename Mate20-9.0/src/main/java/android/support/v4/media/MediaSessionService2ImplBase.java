package android.support.v4.media;

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.GuardedBy;
import android.support.v4.media.MediaSessionService2;
import android.util.Log;

class MediaSessionService2ImplBase implements MediaSessionService2.SupportLibraryImpl {
    private static final boolean DEBUG = true;
    private static final String TAG = "MSS2ImplBase";
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private MediaSession2 mSession;

    MediaSessionService2ImplBase() {
    }

    public void onCreate(MediaSessionService2 service) {
        SessionToken2 token = new SessionToken2(service, new ComponentName(service, service.getClass().getName()));
        if (token.getType() == getSessionType()) {
            MediaSession2 session = service.onCreateSession(token.getId());
            synchronized (this.mLock) {
                this.mSession = session;
                if (this.mSession == null || !token.getId().equals(this.mSession.getToken().getId()) || this.mSession.getToken().getType() != getSessionType()) {
                    this.mSession = null;
                    throw new RuntimeException("Expected session with id " + token.getId() + " and type " + token.getType() + ", but got " + this.mSession);
                }
            }
            return;
        }
        throw new RuntimeException("Expected session type " + getSessionType() + " but was " + token.getType());
    }

    public IBinder onBind(Intent intent) {
        if (MediaSessionService2.SERVICE_INTERFACE.equals(intent.getAction())) {
            synchronized (this.mLock) {
                if (this.mSession != null) {
                    IBinder sessionBinder = this.mSession.getSessionBinder();
                    return sessionBinder;
                }
                Log.d(TAG, "Session hasn't created");
            }
        }
        return null;
    }

    public MediaSessionService2.MediaNotification onUpdateNotification() {
        return null;
    }

    public MediaSession2 getSession() {
        MediaSession2 mediaSession2;
        synchronized (this.mLock) {
            mediaSession2 = this.mSession;
        }
        return mediaSession2;
    }

    public int getSessionType() {
        return 1;
    }
}
