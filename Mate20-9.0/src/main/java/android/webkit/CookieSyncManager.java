package android.webkit;

import android.content.Context;

@Deprecated
public final class CookieSyncManager extends WebSyncManager {
    private static boolean sGetInstanceAllowed = false;
    private static final Object sLock = new Object();
    private static CookieSyncManager sRef;

    public /* bridge */ /* synthetic */ void run() {
        super.run();
    }

    private CookieSyncManager() {
        super(null, null);
    }

    public static CookieSyncManager getInstance() {
        CookieSyncManager cookieSyncManager;
        synchronized (sLock) {
            checkInstanceIsAllowed();
            if (sRef == null) {
                sRef = new CookieSyncManager();
            }
            cookieSyncManager = sRef;
        }
        return cookieSyncManager;
    }

    public static CookieSyncManager createInstance(Context context) {
        CookieSyncManager instance;
        synchronized (sLock) {
            if (context != null) {
                setGetInstanceIsAllowed();
                instance = getInstance();
            } else {
                throw new IllegalArgumentException("Invalid context argument");
            }
        }
        return instance;
    }

    @Deprecated
    public void sync() {
        CookieManager.getInstance().flush();
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void syncFromRamToFlash() {
        CookieManager.getInstance().flush();
    }

    @Deprecated
    public void resetSync() {
    }

    @Deprecated
    public void startSync() {
    }

    @Deprecated
    public void stopSync() {
    }

    static void setGetInstanceIsAllowed() {
        sGetInstanceAllowed = true;
    }

    private static void checkInstanceIsAllowed() {
        if (!sGetInstanceAllowed) {
            throw new IllegalStateException("CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()");
        }
    }
}
