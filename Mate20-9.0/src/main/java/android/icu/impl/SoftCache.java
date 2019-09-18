package android.icu.impl;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SoftCache<K, V, D> extends CacheBase<K, V, D> {
    private ConcurrentHashMap<K, Object> map = new ConcurrentHashMap<>();

    public final V getInstance(K key, D data) {
        Object mapValue = this.map.get(key);
        if (mapValue == null) {
            V value = createInstance(key, data);
            Object mapValue2 = this.map.putIfAbsent(key, (value == null || !CacheValue.futureInstancesWillBeStrong()) ? CacheValue.getInstance(value) : value);
            if (mapValue2 == null) {
                return value;
            }
            if (!(mapValue2 instanceof CacheValue)) {
                return mapValue2;
            }
            return ((CacheValue) mapValue2).resetIfCleared(value);
        } else if (!(mapValue instanceof CacheValue)) {
            return mapValue;
        } else {
            CacheValue<V> cv = (CacheValue) mapValue;
            if (cv.isNull()) {
                return null;
            }
            V value2 = cv.get();
            if (value2 != null) {
                return value2;
            }
            return cv.resetIfCleared(createInstance(key, data));
        }
    }
}
