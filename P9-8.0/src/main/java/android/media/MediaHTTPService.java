package android.media;

import android.media.IMediaHTTPService.Stub;
import android.os.IBinder;
import android.util.Log;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

public class MediaHTTPService extends Stub {
    private static final String TAG = "MediaHTTPService";
    private Boolean mCookieStoreInitialized = new Boolean(false);
    private List<HttpCookie> mCookies;

    public MediaHTTPService(List<HttpCookie> cookies) {
        this.mCookies = cookies;
        Log.v(TAG, "MediaHTTPService(" + this + "): Cookies: " + cookies);
    }

    public IMediaHTTPConnection makeHTTPConnection() {
        synchronized (this.mCookieStoreInitialized) {
            if (!this.mCookieStoreInitialized.booleanValue()) {
                CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                if (cookieManager == null) {
                    cookieManager = new CookieManager();
                    CookieHandler.setDefault(cookieManager);
                    Log.v(TAG, "makeHTTPConnection: CookieManager created: " + cookieManager);
                } else {
                    Log.v(TAG, "makeHTTPConnection: CookieManager(" + cookieManager + ") exists.");
                }
                if (this.mCookies != null) {
                    CookieStore store = cookieManager.getCookieStore();
                    for (HttpCookie cookie : this.mCookies) {
                        try {
                            store.add(null, cookie);
                        } catch (Exception e) {
                            Log.v(TAG, "makeHTTPConnection: CookieStore.add" + e);
                        }
                    }
                }
                this.mCookieStoreInitialized = Boolean.valueOf(true);
                Log.v(TAG, "makeHTTPConnection(" + this + "): cookieManager: " + cookieManager + " Cookies: " + this.mCookies);
            }
        }
        return new MediaHTTPConnection();
    }

    static IBinder createHttpServiceBinderIfNecessary(String path) {
        return createHttpServiceBinderIfNecessary(path, null);
    }

    static IBinder createHttpServiceBinderIfNecessary(String path, List<HttpCookie> cookies) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return new MediaHTTPService(cookies).asBinder();
        }
        if (path.startsWith("widevine://")) {
            Log.d(TAG, "Widevine classic is no longer supported");
        }
        return null;
    }
}
