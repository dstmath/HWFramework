package android.support.v4.media.session;

import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.media.session.MediaSessionCompatApi23;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

/* access modifiers changed from: package-private */
@RequiresApi(24)
public class MediaSessionCompatApi24 {
    private static final String TAG = "MediaSessionCompatApi24";

    public interface Callback extends MediaSessionCompatApi23.Callback {
        void onPrepare();

        void onPrepareFromMediaId(String str, Bundle bundle);

        void onPrepareFromSearch(String str, Bundle bundle);

        void onPrepareFromUri(Uri uri, Bundle bundle);
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    public static String getCallingPackage(Object sessionObj) {
        MediaSession session = (MediaSession) sessionObj;
        try {
            return (String) session.getClass().getMethod("getCallingPackage", new Class[0]).invoke(session, new Object[0]);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "Cannot execute MediaSession.getCallingPackage()", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class CallbackProxy<T extends Callback> extends MediaSessionCompatApi23.CallbackProxy<T> {
        public CallbackProxy(T callback) {
            super(callback);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepare() {
            ((Callback) this.mCallback).onPrepare();
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromMediaId(mediaId, extras);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepareFromSearch(String query, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromSearch(query, extras);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromUri(uri, extras);
        }
    }

    private MediaSessionCompatApi24() {
    }
}
