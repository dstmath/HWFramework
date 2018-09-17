package android.webkit;

import android.webkit.CacheManager.CacheResult;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@Deprecated
public final class UrlInterceptRegistry {
    private static final String LOGTAG = "intercept";
    private static boolean mDisabled = false;
    private static LinkedList mHandlerList;

    private static synchronized LinkedList getHandlers() {
        LinkedList linkedList;
        synchronized (UrlInterceptRegistry.class) {
            if (mHandlerList == null) {
                mHandlerList = new LinkedList();
            }
            linkedList = mHandlerList;
        }
        return linkedList;
    }

    @Deprecated
    public static synchronized void setUrlInterceptDisabled(boolean disabled) {
        synchronized (UrlInterceptRegistry.class) {
            mDisabled = disabled;
        }
    }

    @Deprecated
    public static synchronized boolean urlInterceptDisabled() {
        boolean z;
        synchronized (UrlInterceptRegistry.class) {
            z = mDisabled;
        }
        return z;
    }

    @Deprecated
    public static synchronized boolean registerHandler(UrlInterceptHandler handler) {
        synchronized (UrlInterceptRegistry.class) {
            if (getHandlers().contains(handler)) {
                return false;
            }
            getHandlers().addFirst(handler);
            return true;
        }
    }

    @Deprecated
    public static synchronized boolean unregisterHandler(UrlInterceptHandler handler) {
        boolean remove;
        synchronized (UrlInterceptRegistry.class) {
            remove = getHandlers().remove(handler);
        }
        return remove;
    }

    @Deprecated
    public static synchronized CacheResult getSurrogate(String url, Map<String, String> headers) {
        synchronized (UrlInterceptRegistry.class) {
            if (urlInterceptDisabled()) {
                return null;
            }
            Iterator iter = getHandlers().listIterator();
            while (iter.hasNext()) {
                CacheResult result = ((UrlInterceptHandler) iter.next()).service(url, headers);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    @Deprecated
    public static synchronized PluginData getPluginData(String url, Map<String, String> headers) {
        synchronized (UrlInterceptRegistry.class) {
            if (urlInterceptDisabled()) {
                return null;
            }
            Iterator iter = getHandlers().listIterator();
            while (iter.hasNext()) {
                PluginData data = ((UrlInterceptHandler) iter.next()).getPluginData(url, headers);
                if (data != null) {
                    return data;
                }
            }
            return null;
        }
    }
}
