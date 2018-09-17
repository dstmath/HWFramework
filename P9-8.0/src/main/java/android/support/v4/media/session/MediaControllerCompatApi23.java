package android.support.v4.media.session;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

@RequiresApi(23)
class MediaControllerCompatApi23 {

    public static class TransportControls extends android.support.v4.media.session.MediaControllerCompatApi21.TransportControls {
        public static void playFromUri(Object controlsObj, Uri uri, Bundle extras) {
            ((android.media.session.MediaController.TransportControls) controlsObj).playFromUri(uri, extras);
        }
    }

    MediaControllerCompatApi23() {
    }
}
