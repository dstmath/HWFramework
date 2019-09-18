package android.media;

import android.util.Log;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

public class Media2HTTPService {
    private static final String TAG = "Media2HTTPService";
    private Boolean mCookieStoreInitialized = new Boolean(false);
    private List<HttpCookie> mCookies;

    public Media2HTTPService(List<HttpCookie> cookies) {
        this.mCookies = cookies;
        Log.v(TAG, "Media2HTTPService(" + this + "): Cookies: " + cookies);
    }

    public Media2HTTPConnection makeHTTPConnection() {
        synchronized (this.mCookieStoreInitialized) {
            if (!this.mCookieStoreInitialized.booleanValue()) {
                CookieHandler cookieHandler = CookieHandler.getDefault();
                if (cookieHandler == null) {
                    cookieHandler = new CookieManager();
                    CookieHandler.setDefault(cookieHandler);
                    Log.v(TAG, "makeHTTPConnection: CookieManager created: " + cookieHandler);
                } else {
                    Log.v(TAG, "makeHTTPConnection: CookieHandler (" + cookieHandler + ") exists.");
                }
                if (this.mCookies != null) {
                    if (cookieHandler instanceof CookieManager) {
                        CookieStore store = ((CookieManager) cookieHandler).getCookieStore();
                        for (HttpCookie cookie : this.mCookies) {
                            try {
                                store.add(null, cookie);
                            } catch (Exception e) {
                                Log.v(TAG, "makeHTTPConnection: CookieStore.add" + e);
                            }
                        }
                    } else {
                        Log.w(TAG, "makeHTTPConnection: The installed CookieHandler is not a CookieManager. Can’t add the provided cookies to the cookie store.");
                    }
                }
                this.mCookieStoreInitialized = true;
                Log.v(TAG, "makeHTTPConnection(" + this + "): cookieHandler: " + cookieHandler + " Cookies: " + this.mCookies);
            }
        }
        return new Media2HTTPConnection();
    }

    static Media2HTTPService createHTTPService(String path) {
        return createHTTPService(path, null);
    }

    static Media2HTTPService createHTTPService(String path, List<HttpCookie> cookies) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return new Media2HTTPService(cookies);
        }
        if (path.startsWith("widevine://")) {
            Log.d(TAG, "Widevine classic is no longer supported");
        }
        return null;
    }
}
