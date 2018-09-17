package android.icu.impl;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SoftCache<K, V, D> extends CacheBase<K, V, D> {
    private ConcurrentHashMap<K, Object> map = new ConcurrentHashMap();

    public final V getInstance(K key, D data) {
        CacheValue<V> mapValue = this.map.get(key);
        V value;
        if (mapValue == null) {
            value = createInstance(key, data);
            Object mapValue2 = (value == null || !CacheValue.futureInstancesWillBeStrong()) ? CacheValue.getInstance(value) : value;
            mapValue2 = this.map.putIfAbsent(key, mapValue2);
            if (mapValue2 == null) {
                return value;
            }
            if (mapValue2 instanceof CacheValue) {
                return ((CacheValue) mapValue2).resetIfCleared(value);
            }
            return mapValue2;
        } else if (!(mapValue instanceof CacheValue)) {
            return mapValue;
        } else {
            CacheValue<V> cv = mapValue;
            if (cv.isNull()) {
                return null;
            }
            value = cv.get();
            if (value != null) {
                return value;
            }
            return cv.resetIfCleared(createInstance(key, data));
        }
    }
}
