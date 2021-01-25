package com.huawei.odmf.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ODMFCache<K, V> implements Cache<K, V> {
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_TABLE_SIZE = 16;
    private int currentNum;
    private int hitCount;
    private final Object lock;
    private LinkedHashMap<K, V> map;
    private int maxNum;
    private int missCount;

    static /* synthetic */ int access$010(ODMFCache oDMFCache) {
        int i = oDMFCache.currentNum;
        oDMFCache.currentNum = i - 1;
        return i;
    }

    public ODMFCache() {
        this.lock = new Object();
        this.maxNum = 1000;
        this.currentNum = 0;
        this.map = new LinkedHashMap<K, V>(16, DEFAULT_LOAD_FACTOR, true) {
            /* class com.huawei.odmf.utils.ODMFCache.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // java.util.LinkedHashMap
            public boolean removeEldestEntry(Map.Entry<K, V> entry) {
                boolean z = ODMFCache.this.currentNum >= ODMFCache.this.maxNum;
                if (z) {
                    ODMFCache.access$010(ODMFCache.this);
                }
                return z;
            }
        };
    }

    public ODMFCache(final int i) {
        this.lock = new Object();
        this.maxNum = 1000;
        this.currentNum = 0;
        if (i > 0) {
            this.maxNum = i;
            this.map = new LinkedHashMap<K, V>(16, DEFAULT_LOAD_FACTOR, true) {
                /* class com.huawei.odmf.utils.ODMFCache.AnonymousClass2 */

                /* access modifiers changed from: protected */
                @Override // java.util.LinkedHashMap
                public boolean removeEldestEntry(Map.Entry<K, V> entry) {
                    boolean z = ODMFCache.this.currentNum >= i;
                    if (z) {
                        ODMFCache.access$010(ODMFCache.this);
                    }
                    return z;
                }
            };
            return;
        }
        throw new IllegalArgumentException("maxNum<=0||maxSize <= 0");
    }

    @Override // com.huawei.odmf.utils.Cache
    public V put(K k, V v) {
        V put;
        if (k == null || v == null) {
            throw new IllegalArgumentException("key == null || value == null");
        }
        synchronized (this.lock) {
            put = this.map.put(k, v);
            this.currentNum++;
            if (put != null) {
                this.currentNum--;
            }
        }
        return put;
    }

    @Override // com.huawei.odmf.utils.Cache
    public V get(K k) {
        if (k != null) {
            synchronized (this.lock) {
                V v = this.map.get(k);
                if (v != null) {
                    this.hitCount++;
                    return v;
                }
                this.missCount++;
                return null;
            }
        }
        throw new IllegalArgumentException("key == null");
    }

    @Override // com.huawei.odmf.utils.Cache
    public final V remove(K k) {
        V remove;
        if (k != null) {
            synchronized (this.lock) {
                remove = this.map.remove(k);
                if (remove != null) {
                    this.currentNum--;
                }
            }
            return remove;
        }
        throw new NullPointerException("key == null");
    }

    @Override // com.huawei.odmf.utils.Cache
    public boolean clear() {
        synchronized (this.lock) {
            this.currentNum = 0;
            this.hitCount = 0;
            this.missCount = 0;
            this.map.clear();
        }
        return true;
    }

    public int capacity() {
        int i;
        synchronized (this.lock) {
            i = this.maxNum;
        }
        return i;
    }

    public int size() {
        int i;
        synchronized (this.lock) {
            i = this.currentNum;
        }
        return i;
    }

    @Override // com.huawei.odmf.utils.Cache
    public boolean containsKey(K k) {
        boolean contains;
        if (k != null) {
            synchronized (this.lock) {
                contains = this.map.keySet().contains(k);
            }
            return contains;
        }
        throw new IllegalArgumentException("key == null || value == null");
    }

    @Override // com.huawei.odmf.utils.Cache
    public boolean containsValue(V v) {
        boolean contains;
        if (v != null) {
            synchronized (this.lock) {
                contains = this.map.values().contains(v);
            }
            return contains;
        }
        throw new IllegalArgumentException("key == null || value == null");
    }

    public int getHitRate() {
        int i;
        synchronized (this.lock) {
            int i2 = this.hitCount + this.missCount;
            i = i2 != 0 ? (this.hitCount * 100) / i2 : 0;
        }
        return i;
    }
}
