package android.support.v4.media.session;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

@RequiresApi(24)
class MediaControllerCompatApi24 {

    public static class TransportControls extends android.support.v4.media.session.MediaControllerCompatApi23.TransportControls {
        public static void prepare(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).prepare();
        }

        public static void prepareFromMediaId(Object controlsObj, String mediaId, Bundle extras) {
            ((android.media.session.MediaController.TransportControls) controlsObj).prepareFromMediaId(mediaId, extras);
        }

        public static void prepareFromSearch(Object controlsObj, String query, Bundle extras) {
            ((android.media.session.MediaController.TransportControls) controlsObj).prepareFromSearch(query, extras);
        }

        public static void prepareFromUri(Object controlsObj, Uri uri, Bundle extras) {
            ((android.media.session.MediaController.TransportControls) controlsObj).prepareFromUri(uri, extras);
        }
    }

    MediaControllerCompatApi24() {
    }
}
