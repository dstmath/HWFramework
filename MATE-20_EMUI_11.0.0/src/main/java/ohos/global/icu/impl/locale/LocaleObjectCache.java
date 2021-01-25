package ohos.global.icu.impl.locale;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LocaleObjectCache<K, V> {
    private ConcurrentHashMap<K, CacheEntry<K, V>> _map;
    private ReferenceQueue<V> _queue;

    /* access modifiers changed from: protected */
    public abstract V createObject(K k);

    /* access modifiers changed from: protected */
    public K normalizeKey(K k) {
        return k;
    }

    public LocaleObjectCache() {
        this(16, 0.75f, 16);
    }

    public LocaleObjectCache(int i, float f, int i2) {
        this._queue = new ReferenceQueue<>();
        this._map = new ConcurrentHashMap<>(i, f, i2);
    }

    public V get(K k) {
        cleanStaleEntries();
        CacheEntry<K, V> cacheEntry = this._map.get(k);
        V v = cacheEntry != null ? (V) cacheEntry.get() : null;
        if (v != null) {
            return v;
        }
        K normalizeKey = normalizeKey(k);
        V createObject = createObject(normalizeKey);
        if (normalizeKey == null || createObject == null) {
            return null;
        }
        CacheEntry<K, V> cacheEntry2 = new CacheEntry<>(normalizeKey, createObject, this._queue);
        while (v == null) {
            cleanStaleEntries();
            CacheEntry<K, V> putIfAbsent = this._map.putIfAbsent(normalizeKey, cacheEntry2);
            if (putIfAbsent == null) {
                return createObject;
            }
            v = (V) putIfAbsent.get();
        }
        return v;
    }

    private void cleanStaleEntries() {
        while (true) {
            CacheEntry cacheEntry = (CacheEntry) this._queue.poll();
            if (cacheEntry != null) {
                this._map.remove(cacheEntry.getKey());
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CacheEntry<K, V> extends SoftReference<V> {
        private K _key;

        CacheEntry(K k, V v, ReferenceQueue<V> referenceQueue) {
            super(v, referenceQueue);
            this._key = k;
        }

        /* access modifiers changed from: package-private */
        public K getKey() {
            return this._key;
        }
    }
}
