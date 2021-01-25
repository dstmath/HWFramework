package ohos.global.icu.impl;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SoftCache<K, V, D> extends CacheBase<K, V, D> {
    private ConcurrentHashMap<K, Object> map = new ConcurrentHashMap<>();

    @Override // ohos.global.icu.impl.CacheBase
    public final V getInstance(K k, D d) {
        CacheValue cacheValue;
        V v = (V) this.map.get(k);
        if (v == null) {
            V createInstance = createInstance(k, d);
            if (createInstance == null || !CacheValue.futureInstancesWillBeStrong()) {
                cacheValue = CacheValue.getInstance(createInstance);
            } else {
                cacheValue = createInstance;
            }
            V v2 = (V) this.map.putIfAbsent(k, cacheValue);
            if (v2 == null) {
                return createInstance;
            }
            return !(v2 instanceof CacheValue) ? v2 : (V) v2.resetIfCleared(createInstance);
        } else if (!(v instanceof CacheValue)) {
            return v;
        } else {
            V v3 = v;
            if (v3.isNull()) {
                return null;
            }
            V v4 = (V) v3.get();
            return v4 != null ? v4 : (V) v3.resetIfCleared(createInstance(k, d));
        }
    }
}
