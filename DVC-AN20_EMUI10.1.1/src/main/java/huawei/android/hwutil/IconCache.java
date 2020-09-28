package huawei.android.hwutil;

import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IconCache {
    static final String TAG_CACHE = "Icon_Cache";
    private static final HashMap<String, CacheEntry> mCache = new HashMap<>();

    public static class CacheEntry {
        public String name;
        public int type;
    }

    public static void add(String idAndPackageName, CacheEntry entry) {
        synchronized (mCache) {
            if (!mCache.containsKey(idAndPackageName)) {
                mCache.put(idAndPackageName, entry);
            }
        }
    }

    public static boolean contains(String idAndPackageName) {
        boolean containsKey;
        synchronized (mCache) {
            containsKey = mCache.containsKey(idAndPackageName);
        }
        return containsKey;
    }

    public static CacheEntry get(String idAndPackageName) {
        CacheEntry cacheEntry;
        synchronized (mCache) {
            cacheEntry = mCache.get(idAndPackageName);
        }
        return cacheEntry;
    }

    public static void remove(String idAndPackageName) {
        synchronized (mCache) {
            mCache.remove(idAndPackageName);
        }
    }

    public static boolean removeByPackageName(String packageName) {
        synchronized (mCache) {
            Iterator<Map.Entry<String, CacheEntry>> it = mCache.entrySet().iterator();
            while (it.hasNext()) {
                String key = it.next().getKey();
                if (key.endsWith(packageName)) {
                    Log.i(TAG_CACHE, "removeByPackageName: key = " + key);
                    it.remove();
                }
            }
        }
        return true;
    }
}
