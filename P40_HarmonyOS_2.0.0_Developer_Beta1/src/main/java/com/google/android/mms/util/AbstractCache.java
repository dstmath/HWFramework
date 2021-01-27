package com.google.android.mms.util;

import java.util.HashMap;

public abstract class AbstractCache<K, V> {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final int MAX_CACHED_ITEMS = 500;
    private static final String TAG = "AbstractCache";
    private final HashMap<K, CacheEntry<V>> mCacheMap = new HashMap<>();

    protected AbstractCache() {
    }

    public boolean put(K key, V value) {
        if (this.mCacheMap.size() >= MAX_CACHED_ITEMS || key == null) {
            return false;
        }
        CacheEntry<V> cacheEntry = new CacheEntry<>();
        cacheEntry.value = value;
        this.mCacheMap.put(key, cacheEntry);
        return true;
    }

    public V get(K key) {
        CacheEntry<V> cacheEntry;
        if (key == null || (cacheEntry = this.mCacheMap.get(key)) == null) {
            return null;
        }
        cacheEntry.hit++;
        return cacheEntry.value;
    }

    public V purge(K key) {
        CacheEntry<V> v = this.mCacheMap.remove(key);
        if (v != null) {
            return v.value;
        }
        return null;
    }

    public void purgeAll() {
        this.mCacheMap.clear();
    }

    public int size() {
        return this.mCacheMap.size();
    }

    private static class CacheEntry<V> {
        int hit;
        V value;

        private CacheEntry() {
        }
    }
}
