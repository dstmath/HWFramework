package android.icu.impl.locale;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LocaleObjectCache<K, V> {
    private ConcurrentHashMap<K, CacheEntry<K, V>> _map;
    private ReferenceQueue<V> _queue;

    private static class CacheEntry<K, V> extends SoftReference<V> {
        private K _key;

        CacheEntry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this._key = key;
        }

        K getKey() {
            return this._key;
        }
    }

    protected abstract V createObject(K k);

    public LocaleObjectCache() {
        this(16, 0.75f, 16);
    }

    public LocaleObjectCache(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this._queue = new ReferenceQueue();
        this._map = new ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
    }

    public V get(K key) {
        V value = null;
        cleanStaleEntries();
        CacheEntry<K, V> entry = (CacheEntry) this._map.get(key);
        if (entry != null) {
            value = entry.get();
        }
        if (value == null) {
            key = normalizeKey(key);
            V newVal = createObject(key);
            if (key == null || newVal == null) {
                return null;
            }
            CacheEntry<K, V> newEntry = new CacheEntry(key, newVal, this._queue);
            while (value == null) {
                cleanStaleEntries();
                entry = (CacheEntry) this._map.putIfAbsent(key, newEntry);
                if (entry == null) {
                    value = newVal;
                    break;
                }
                value = entry.get();
            }
        }
        return value;
    }

    private void cleanStaleEntries() {
        while (true) {
            CacheEntry<K, V> entry = (CacheEntry) this._queue.poll();
            if (entry != null) {
                this._map.remove(entry.getKey());
            } else {
                return;
            }
        }
    }

    protected K normalizeKey(K key) {
        return key;
    }
}
