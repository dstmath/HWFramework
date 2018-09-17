package huawei.android.hwutil;

import java.util.HashMap;

public class IconCache {
    private static final HashMap<String, CacheEntry> mCache = new HashMap();

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
            cacheEntry = (CacheEntry) mCache.get(idAndPackageName);
        }
        return cacheEntry;
    }

    public static void remove(String idAndPackageName) {
        synchronized (mCache) {
            mCache.remove(idAndPackageName);
        }
    }
}
