package ohos.global.resource;

import java.util.LinkedHashMap;
import java.util.Map;

/* compiled from: ResourceManagerImpl */
class CacheMap<K, V> {
    private static final float DEFAULT_LOAD_FACTORY = 1.0f;
    private static final int DEFAULT_MAX_CACHE_SIZE = 10;
    LinkedHashMap<K, V> map;

    public CacheMap() {
        this(10);
    }

    public CacheMap(final int i) {
        this.map = new LinkedHashMap<K, V>(1.0f, true, i) {
            /* class ohos.global.resource.CacheMap.AnonymousClass1 */
            private static final long serialVersionUID = 1;

            /* access modifiers changed from: protected */
            @Override // java.util.LinkedHashMap
            public boolean removeEldestEntry(Map.Entry<K, V> entry) {
                return size() > i;
            }
        };
    }

    /* access modifiers changed from: package-private */
    public synchronized void put(K k, V v) {
        this.map.put(k, v);
    }

    /* access modifiers changed from: package-private */
    public synchronized V get(K k) {
        return this.map.get(k);
    }

    /* access modifiers changed from: package-private */
    public synchronized void remove(K k) {
        this.map.remove(k);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean containsKey(K k) {
        return this.map.containsKey(k);
    }

    /* access modifiers changed from: package-private */
    public synchronized void clear() {
        this.map.clear();
    }

    public String toString() {
        return this.map.toString();
    }
}
