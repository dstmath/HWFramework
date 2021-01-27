package huawei.android.hwutil;

import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IconCache {
    private static final HashMap<String, CacheEntry> ICON_CACHE = new HashMap<>();
    static final String TAG_CACHE = "Icon_Cache";

    public static class CacheEntry {
        public String name;
        public int type;
    }

    public static void add(String idAndPackageName, CacheEntry entry) {
        synchronized (ICON_CACHE) {
            if (!ICON_CACHE.containsKey(idAndPackageName)) {
                ICON_CACHE.put(idAndPackageName, entry);
            }
        }
    }

    public static boolean contains(String idAndPackageName) {
        boolean containsKey;
        synchronized (ICON_CACHE) {
            containsKey = ICON_CACHE.containsKey(idAndPackageName);
        }
        return containsKey;
    }

    public static CacheEntry get(String idAndPackageName) {
        CacheEntry cacheEntry;
        synchronized (ICON_CACHE) {
            cacheEntry = ICON_CACHE.get(idAndPackageName);
        }
        return cacheEntry;
    }

    public static void remove(String idAndPackageName) {
        synchronized (ICON_CACHE) {
            if (idAndPackageName != null) {
                ICON_CACHE.remove(idAndPackageName);
            }
        }
    }

    public static boolean removeByPackageName(String packageName) {
        synchronized (ICON_CACHE) {
            if (packageName == null) {
                Log.e(TAG_CACHE, "Remove iconCache error, packageName is null");
                return false;
            }
            Iterator<Map.Entry<String, CacheEntry>> it = ICON_CACHE.entrySet().iterator();
            while (it.hasNext()) {
                String key = it.next().getKey();
                if (key.endsWith(packageName)) {
                    Log.i(TAG_CACHE, "removeByPackageName: key = " + key);
                    it.remove();
                }
            }
            return true;
        }
    }
}
