package android.support.v4.media;

import android.content.Intent;
import android.os.IBinder;
import android.support.v4.media.MediaLibraryService2;

class MediaLibraryService2ImplBase extends MediaSessionService2ImplBase {
    MediaLibraryService2ImplBase() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003d  */
    public IBinder onBind(Intent intent) {
        char c;
        String action = intent.getAction();
        int hashCode = action.hashCode();
        if (hashCode == 901933117) {
            if (action.equals(MediaLibraryService2.SERVICE_INTERFACE)) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        } else if (hashCode == 1665850838 && action.equals(MediaBrowserServiceCompat.SERVICE_INTERFACE)) {
            c = 1;
            switch (c) {
                case 0:
                    return getSession().getSessionBinder();
                case 1:
                    return getSession().getImpl().getLegacySessionBinder();
                default:
                    return super.onBind(intent);
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
    }

    public MediaLibraryService2.MediaLibrarySession getSession() {
        return (MediaLibraryService2.MediaLibrarySession) super.getSession();
    }

    public int getSessionType() {
        return 2;
    }
}
