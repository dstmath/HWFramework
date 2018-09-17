package android.support.v4.media.session;

import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

@RequiresApi(24)
class MediaSessionCompatApi24 {
    private static final String TAG = "MediaSessionCompatApi24";

    public interface Callback extends android.support.v4.media.session.MediaSessionCompatApi23.Callback {
        void onPrepare();

        void onPrepareFromMediaId(String str, Bundle bundle);

        void onPrepareFromSearch(String str, Bundle bundle);

        void onPrepareFromUri(Uri uri, Bundle bundle);
    }

    static class CallbackProxy<T extends Callback> extends CallbackProxy<T> {
        public CallbackProxy(T callback) {
            super(callback);
        }

        public void onPrepare() {
            ((Callback) this.mCallback).onPrepare();
        }

        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromMediaId(mediaId, extras);
        }

        public void onPrepareFromSearch(String query, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromSearch(query, extras);
        }

        public void onPrepareFromUri(Uri uri, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromUri(uri, extras);
        }
    }

    MediaSessionCompatApi24() {
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x001b A:{Splitter: B:1:0x0003, ExcHandler: java.lang.NoSuchMethodException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001b A:{Splitter: B:1:0x0003, ExcHandler: java.lang.NoSuchMethodException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:4:0x001b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x001c, code:
            android.util.Log.e(TAG, "Cannot execute MediaSession.getCallingPackage()", r0);
     */
    /* JADX WARNING: Missing block: B:6:0x0026, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getCallingPackage(Object sessionObj) {
        MediaSession session = (MediaSession) sessionObj;
        try {
            return (String) session.getClass().getMethod("getCallingPackage", new Class[0]).invoke(session, new Object[0]);
        } catch (ReflectiveOperationException e) {
        }
    }
}
