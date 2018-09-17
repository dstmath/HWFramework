package libcore.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BasicLruCache<K, V> {
    private final LinkedHashMap<K, V> map;
    private final int maxSize;

    public BasicLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap(0, 0.75f, true);
    }

    /* JADX WARNING: Missing block: B:10:0x0017, code:
            r0 = create(r4);
     */
    /* JADX WARNING: Missing block: B:11:0x001b, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:12:0x001c, code:
            if (r0 == null) goto L_0x0028;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r3.map.put(r4, r0);
            trimToSize(r3.maxSize);
     */
    /* JADX WARNING: Missing block: B:15:0x0028, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:16:0x0029, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            V result = this.map.get(key);
            if (result != null) {
                return result;
            }
        }
    }

    public final synchronized V put(K key, V value) {
        V previous;
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        } else {
            previous = this.map.put(key, value);
            trimToSize(this.maxSize);
        }
        return previous;
    }

    private void trimToSize(int maxSize) {
        while (this.map.size() > maxSize) {
            Entry<K, V> toEvict = this.map.eldest();
            K key = toEvict.getKey();
            V value = toEvict.getValue();
            this.map.remove(key);
            entryEvicted(key, value);
        }
    }

    protected void entryEvicted(K k, V v) {
    }

    protected V create(K k) {
        return null;
    }

    public final synchronized Map<K, V> snapshot() {
        return new LinkedHashMap(this.map);
    }

    public final synchronized void evictAll() {
        trimToSize(0);
    }
}
