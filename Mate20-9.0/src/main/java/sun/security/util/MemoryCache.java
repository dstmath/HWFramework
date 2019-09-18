package sun.security.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import sun.security.util.Cache;

/* compiled from: Cache */
class MemoryCache<K, V> extends Cache<K, V> {
    private static final boolean DEBUG = false;
    private static final float LOAD_FACTOR = 0.75f;
    private final Map<K, CacheEntry<K, V>> cacheMap;
    private long lifetime;
    private int maxSize;
    private final ReferenceQueue<V> queue;

    /* compiled from: Cache */
    private interface CacheEntry<K, V> {
        K getKey();

        V getValue();

        void invalidate();

        boolean isValid(long j);
    }

    /* compiled from: Cache */
    private static class HardCacheEntry<K, V> implements CacheEntry<K, V> {
        private long expirationTime;
        private K key;
        private V value;

        HardCacheEntry(K key2, V value2, long expirationTime2) {
            this.key = key2;
            this.value = value2;
            this.expirationTime = expirationTime2;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public boolean isValid(long currentTime) {
            boolean valid = currentTime <= this.expirationTime ? true : MemoryCache.DEBUG;
            if (!valid) {
                invalidate();
            }
            return valid;
        }

        public void invalidate() {
            this.key = null;
            this.value = null;
            this.expirationTime = -1;
        }
    }

    /* compiled from: Cache */
    private static class SoftCacheEntry<K, V> extends SoftReference<V> implements CacheEntry<K, V> {
        private long expirationTime;
        private K key;

        SoftCacheEntry(K key2, V value, long expirationTime2, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key2;
            this.expirationTime = expirationTime2;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return get();
        }

        public boolean isValid(long currentTime) {
            boolean valid = (currentTime > this.expirationTime || get() == null) ? MemoryCache.DEBUG : true;
            if (!valid) {
                invalidate();
            }
            return valid;
        }

        public void invalidate() {
            clear();
            this.key = null;
            this.expirationTime = -1;
        }
    }

    public MemoryCache(boolean soft, int maxSize2) {
        this(soft, maxSize2, 0);
    }

    public MemoryCache(boolean soft, int maxSize2, int lifetime2) {
        this.maxSize = maxSize2;
        this.lifetime = (long) (lifetime2 * 1000);
        if (soft) {
            this.queue = new ReferenceQueue<>();
        } else {
            this.queue = null;
        }
        this.cacheMap = new LinkedHashMap(((int) (((float) maxSize2) / LOAD_FACTOR)) + 1, LOAD_FACTOR, true);
    }

    private void emptyQueue() {
        if (this.queue != null) {
            int size = this.cacheMap.size();
            while (true) {
                CacheEntry<K, V> entry = (CacheEntry) this.queue.poll();
                if (entry != null) {
                    K key = entry.getKey();
                    if (key != null) {
                        CacheEntry<K, V> currentEntry = this.cacheMap.remove(key);
                        if (!(currentEntry == null || entry == currentEntry)) {
                            this.cacheMap.put(key, currentEntry);
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void expungeExpiredEntries() {
        emptyQueue();
        if (this.lifetime != 0) {
            int cnt = 0;
            long time = System.currentTimeMillis();
            Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
            while (t.hasNext()) {
                if (!t.next().isValid(time)) {
                    t.remove();
                    cnt++;
                }
            }
        }
    }

    public synchronized int size() {
        expungeExpiredEntries();
        return this.cacheMap.size();
    }

    public synchronized void clear() {
        if (this.queue != null) {
            for (CacheEntry<K, V> entry : this.cacheMap.values()) {
                entry.invalidate();
            }
            while (this.queue.poll() != null) {
            }
        }
        this.cacheMap.clear();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0060, code lost:
        return;
     */
    public synchronized void put(K key, V value) {
        emptyQueue();
        long expirationTime = 0;
        if (this.lifetime != 0) {
            expirationTime = this.lifetime + System.currentTimeMillis();
        }
        CacheEntry<K, V> oldEntry = (CacheEntry) this.cacheMap.put(key, newEntry(key, value, expirationTime, this.queue));
        if (oldEntry != null) {
            oldEntry.invalidate();
        } else if (this.maxSize > 0 && this.cacheMap.size() > this.maxSize) {
            expungeExpiredEntries();
            if (this.cacheMap.size() > this.maxSize) {
                Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
                t.remove();
                t.next().invalidate();
            }
        }
    }

    public synchronized V get(Object key) {
        emptyQueue();
        CacheEntry<K, V> entry = this.cacheMap.get(key);
        if (entry == null) {
            return null;
        }
        long time = 0;
        if (this.lifetime != 0) {
            time = System.currentTimeMillis();
        }
        if (!entry.isValid(time)) {
            this.cacheMap.remove(key);
            return null;
        }
        return entry.getValue();
    }

    public synchronized void remove(Object key) {
        emptyQueue();
        CacheEntry<K, V> entry = this.cacheMap.remove(key);
        if (entry != null) {
            entry.invalidate();
        }
    }

    public synchronized void setCapacity(int size) {
        expungeExpiredEntries();
        if (size > 0 && this.cacheMap.size() > size) {
            Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
            for (int i = this.cacheMap.size() - size; i > 0; i--) {
                t.remove();
                t.next().invalidate();
            }
        }
        this.maxSize = size > 0 ? size : 0;
    }

    public synchronized void setTimeout(int timeout) {
        emptyQueue();
        this.lifetime = timeout > 0 ? ((long) timeout) * 1000 : 0;
    }

    public synchronized void accept(Cache.CacheVisitor<K, V> visitor) {
        expungeExpiredEntries();
        visitor.visit(getCachedEntries());
    }

    private Map<K, V> getCachedEntries() {
        Map<K, V> kvmap = new HashMap<>(this.cacheMap.size());
        for (CacheEntry<K, V> entry : this.cacheMap.values()) {
            kvmap.put(entry.getKey(), entry.getValue());
        }
        return kvmap;
    }

    /* access modifiers changed from: protected */
    public CacheEntry<K, V> newEntry(K key, V value, long expirationTime, ReferenceQueue<V> queue2) {
        if (queue2 == null) {
            return new HardCacheEntry(key, value, expirationTime);
        }
        SoftCacheEntry softCacheEntry = new SoftCacheEntry(key, value, expirationTime, queue2);
        return softCacheEntry;
    }
}
