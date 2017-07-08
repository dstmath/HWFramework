package sun.security.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import sun.security.util.Cache.CacheVisitor;
import sun.util.logging.PlatformLogger;

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

        HardCacheEntry(K key, V value, long expirationTime) {
            this.key = key;
            this.value = value;
            this.expirationTime = expirationTime;
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

        SoftCacheEntry(K key, V value, long expirationTime, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
            this.expirationTime = expirationTime;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return get();
        }

        public boolean isValid(long currentTime) {
            boolean valid = MemoryCache.DEBUG;
            if (currentTime <= this.expirationTime && get() != null) {
                valid = true;
            }
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

    public MemoryCache(boolean soft, int maxSize) {
        this(soft, maxSize, 0);
    }

    public MemoryCache(boolean soft, int maxSize, int lifetime) {
        this.maxSize = maxSize;
        this.lifetime = (long) (lifetime * PlatformLogger.SEVERE);
        if (soft) {
            this.queue = new ReferenceQueue();
        } else {
            this.queue = null;
        }
        this.cacheMap = new LinkedHashMap(((int) (((float) maxSize) / LOAD_FACTOR)) + 1, LOAD_FACTOR, true);
    }

    private void emptyQueue() {
        if (this.queue != null) {
            int startSize = this.cacheMap.size();
            while (true) {
                CacheEntry<K, V> entry = (CacheEntry) this.queue.poll();
                if (entry != null) {
                    K key = entry.getKey();
                    if (key != null) {
                        CacheEntry<K, V> currentEntry = (CacheEntry) this.cacheMap.remove(key);
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
                if (!((CacheEntry) t.next()).isValid(time)) {
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
            do {
            } while (this.queue.poll() != null);
        }
        this.cacheMap.clear();
    }

    public synchronized void put(K key, V value) {
        long expirationTime;
        emptyQueue();
        if (this.lifetime == 0) {
            expirationTime = 0;
        } else {
            expirationTime = System.currentTimeMillis() + this.lifetime;
        }
        CacheEntry<K, V> oldEntry = (CacheEntry) this.cacheMap.put(key, newEntry(key, value, expirationTime, this.queue));
        if (oldEntry != null) {
            oldEntry.invalidate();
            return;
        }
        if (this.maxSize > 0 && this.cacheMap.size() > this.maxSize) {
            expungeExpiredEntries();
            if (this.cacheMap.size() > this.maxSize) {
                Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
                CacheEntry<K, V> lruEntry = (CacheEntry) t.next();
                t.remove();
                lruEntry.invalidate();
            }
        }
    }

    public synchronized V get(Object key) {
        long time = 0;
        synchronized (this) {
            emptyQueue();
            CacheEntry<K, V> entry = (CacheEntry) this.cacheMap.get(key);
            if (entry == null) {
                return null;
            }
            if (this.lifetime != 0) {
                time = System.currentTimeMillis();
            }
            if (entry.isValid(time)) {
                V value = entry.getValue();
                return value;
            }
            this.cacheMap.remove(key);
            return null;
        }
    }

    public synchronized void remove(Object key) {
        emptyQueue();
        CacheEntry<K, V> entry = (CacheEntry) this.cacheMap.remove(key);
        if (entry != null) {
            entry.invalidate();
        }
    }

    public synchronized void setCapacity(int size) {
        expungeExpiredEntries();
        if (size > 0 && this.cacheMap.size() > size) {
            Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
            for (int i = this.cacheMap.size() - size; i > 0; i--) {
                CacheEntry<K, V> lruEntry = (CacheEntry) t.next();
                t.remove();
                lruEntry.invalidate();
            }
        }
        if (size <= 0) {
            size = 0;
        }
        this.maxSize = size;
    }

    public synchronized void setTimeout(int timeout) {
        emptyQueue();
        this.lifetime = timeout > 0 ? ((long) timeout) * 1000 : 0;
    }

    public synchronized void accept(CacheVisitor<K, V> visitor) {
        expungeExpiredEntries();
        visitor.visit(getCachedEntries());
    }

    private Map<K, V> getCachedEntries() {
        Map<K, V> kvmap = new HashMap(this.cacheMap.size());
        for (CacheEntry<K, V> entry : this.cacheMap.values()) {
            kvmap.put(entry.getKey(), entry.getValue());
        }
        return kvmap;
    }

    protected CacheEntry<K, V> newEntry(K key, V value, long expirationTime, ReferenceQueue<V> queue) {
        if (queue != null) {
            return new SoftCacheEntry(key, value, expirationTime, queue);
        }
        return new HardCacheEntry(key, value, expirationTime);
    }
}
