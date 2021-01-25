package android.support.v4.media.session;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.media.session.MediaSessionCompatApi21;

/* access modifiers changed from: package-private */
@RequiresApi(23)
public class MediaSessionCompatApi23 {

    public interface Callback extends MediaSessionCompatApi21.Callback {
        void onPlayFromUri(Uri uri, Bundle bundle);
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    /* access modifiers changed from: package-private */
    public static class CallbackProxy<T extends Callback> extends MediaSessionCompatApi21.CallbackProxy<T> {
        public CallbackProxy(T callback) {
            super(callback);
        }

        @Override // android.media.session.MediaSession.Callback
        public void onPlayFromUri(Uri uri, Bundle extras) {
            ((Callback) this.mCallback).onPlayFromUri(uri, extras);
        }
    }

    private MediaSessionCompatApi23() {
    }
}
