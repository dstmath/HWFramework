package ohos.global.icu.impl;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SimpleCache<K, V> implements ICUCache<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private volatile Reference<Map<K, V>> cacheRef;
    private int capacity;
    private int type;

    public SimpleCache() {
        this.cacheRef = null;
        this.type = 0;
        this.capacity = 16;
    }

    public SimpleCache(int i) {
        this(i, 16);
    }

    public SimpleCache(int i, int i2) {
        this.cacheRef = null;
        this.type = 0;
        this.capacity = 16;
        if (i == 1) {
            this.type = i;
        }
        if (i2 > 0) {
            this.capacity = i2;
        }
    }

    @Override // ohos.global.icu.impl.ICUCache
    public V get(Object obj) {
        Map<K, V> map;
        Reference<Map<K, V>> reference = this.cacheRef;
        if (reference == null || (map = reference.get()) == null) {
            return null;
        }
        return map.get(obj);
    }

    @Override // ohos.global.icu.impl.ICUCache
    public void put(K k, V v) {
        Reference<Map<K, V>> reference;
        Reference<Map<K, V>> reference2 = this.cacheRef;
        Map<K, V> map = reference2 != null ? reference2.get() : null;
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap(this.capacity));
            if (this.type == 1) {
                reference = new WeakReference<>(map);
            } else {
                reference = new SoftReference<>(map);
            }
            this.cacheRef = reference;
        }
        map.put(k, v);
    }

    @Override // ohos.global.icu.impl.ICUCache
    public void clear() {
        this.cacheRef = null;
    }
}
