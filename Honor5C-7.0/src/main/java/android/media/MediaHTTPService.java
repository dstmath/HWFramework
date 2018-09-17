package android.media;

import android.media.IMediaHTTPService.Stub;
import android.os.IBinder;

public class MediaHTTPService extends Stub {
    private static final String TAG = "MediaHTTPService";

    public IMediaHTTPConnection makeHTTPConnection() {
        return new MediaHTTPConnection();
    }

    static IBinder createHttpServiceBinderIfNecessary(String path) {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("widevine://")) {
            return new MediaHTTPService().asBinder();
        }
        return null;
    }
}
