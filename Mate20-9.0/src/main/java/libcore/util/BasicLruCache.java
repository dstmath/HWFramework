package libcore.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class BasicLruCache<K, V> {
    private final LinkedHashMap<K, V> map;
    private final int maxSize;

    public BasicLruCache(int maxSize2) {
        if (maxSize2 > 0) {
            this.maxSize = maxSize2;
            this.map = new LinkedHashMap<>(0, 0.75f, true);
            return;
        }
        throw new IllegalArgumentException("maxSize <= 0");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        if (r0 == null) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r2.map.put(r3, r0);
        trimToSize(r2.maxSize);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000e, code lost:
        r0 = create(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        monitor-enter(r2);
     */
    public final V get(K key) {
        if (key != null) {
            synchronized (this) {
                V result = this.map.get(key);
                if (result != null) {
                    return result;
                }
            }
        } else {
            throw new NullPointerException("key == null");
        }
    }

    public final synchronized V put(K key, V value) {
        V previous;
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value != null) {
            previous = this.map.put(key, value);
            trimToSize(this.maxSize);
        } else {
            throw new NullPointerException("value == null");
        }
        return previous;
    }

    private void trimToSize(int maxSize2) {
        while (this.map.size() > maxSize2) {
            Map.Entry<K, V> toEvict = this.map.eldest();
            K key = toEvict.getKey();
            V value = toEvict.getValue();
            this.map.remove(key);
            entryEvicted(key, value);
        }
    }

    /* access modifiers changed from: protected */
    public void entryEvicted(K k, V v) {
    }

    /* access modifiers changed from: protected */
    public V create(K k) {
        return null;
    }

    public final synchronized Map<K, V> snapshot() {
        return new LinkedHashMap(this.map);
    }

    public final synchronized void evictAll() {
        trimToSize(0);
    }
}
