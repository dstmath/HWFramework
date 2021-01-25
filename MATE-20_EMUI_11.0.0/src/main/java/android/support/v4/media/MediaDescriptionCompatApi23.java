package android.support.v4.media;

import android.media.MediaDescription;
import android.net.Uri;
import android.support.annotation.RequiresApi;

/* access modifiers changed from: package-private */
@RequiresApi(23)
public class MediaDescriptionCompatApi23 {
    public static Uri getMediaUri(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getMediaUri();
    }

    /* access modifiers changed from: package-private */
    public static class Builder {
        public static void setMediaUri(Object builderObj, Uri mediaUri) {
            ((MediaDescription.Builder) builderObj).setMediaUri(mediaUri);
        }

        private Builder() {
        }
    }

    private MediaDescriptionCompatApi23() {
    }
}
