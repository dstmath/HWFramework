package android.net;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.util.Log;
import com.android.org.conscrypt.ClientSessionContext;
import com.android.org.conscrypt.FileClientSessionCache;
import com.android.org.conscrypt.SSLClientSessionCache;
import java.io.File;
import java.io.IOException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;

public final class SSLSessionCache {
    private static final String TAG = "SSLSessionCache";
    @UnsupportedAppUsage
    final SSLClientSessionCache mSessionCache;

    public static void install(SSLSessionCache cache, SSLContext context) {
        SSLSessionContext clientContext = context.getClientSessionContext();
        if (clientContext instanceof ClientSessionContext) {
            ((ClientSessionContext) clientContext).setPersistentCache(cache == null ? null : cache.mSessionCache);
            return;
        }
        throw new IllegalArgumentException("Incompatible SSLContext: " + context);
    }

    public SSLSessionCache(Object cache) {
        this.mSessionCache = (SSLClientSessionCache) cache;
    }

    public SSLSessionCache(File dir) throws IOException {
        this.mSessionCache = FileClientSessionCache.usingDirectory(dir);
    }

    public SSLSessionCache(Context context) {
        SSLClientSessionCache cache = null;
        try {
            cache = FileClientSessionCache.usingDirectory(context.getDir("sslcache", 0));
        } catch (IOException e) {
            Log.w(TAG, "Unable to create SSL session cache in.");
        }
        this.mSessionCache = cache;
    }
}
