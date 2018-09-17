package android.media;

import android.content.Context;
import android.net.Uri;
import java.util.Map;

public class HwCustMediaPlayer {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwCustMediaPlayer";

    public Map<String, String> setStreamingMediaHeaders(Context context, Uri uri, Map<String, String> headers) {
        return headers;
    }
}
