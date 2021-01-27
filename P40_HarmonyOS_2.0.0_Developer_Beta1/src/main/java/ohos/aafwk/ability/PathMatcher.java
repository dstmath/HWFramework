package ohos.aafwk.ability;

import java.util.HashMap;

public class PathMatcher {
    public static final int NO_MATCH = -1;
    private HashMap<String, Integer> pathCache;

    public boolean addPath(String str, int i) {
        if (i < 0 || str == null || str.isEmpty()) {
            return false;
        }
        if (this.pathCache == null) {
            this.pathCache = new HashMap<>(1);
        }
        this.pathCache.put(str, Integer.valueOf(i));
        return true;
    }

    public int getPathId(String str) {
        HashMap<String, Integer> hashMap;
        if (str == null || str.isEmpty() || (hashMap = this.pathCache) == null || hashMap.isEmpty() || !this.pathCache.containsKey(str)) {
            return -1;
        }
        return this.pathCache.get(str).intValue();
    }
}
