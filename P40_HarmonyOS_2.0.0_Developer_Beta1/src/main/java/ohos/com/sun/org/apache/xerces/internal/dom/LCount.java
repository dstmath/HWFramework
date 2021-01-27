package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* access modifiers changed from: package-private */
public class LCount {
    static final Map<String, LCount> lCounts = new ConcurrentHashMap();
    public int bubbles = 0;
    public int captures = 0;
    public int defaults;
    public int total = 0;

    LCount() {
    }

    static LCount lookup(String str) {
        LCount lCount = lCounts.get(str);
        if (lCount != null) {
            return lCount;
        }
        Map<String, LCount> map = lCounts;
        LCount lCount2 = new LCount();
        map.put(str, lCount2);
        return lCount2;
    }
}
