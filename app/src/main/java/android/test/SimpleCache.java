package android.test;

import java.util.HashMap;
import java.util.Map;

@Deprecated
abstract class SimpleCache<K, V> {
    private Map<K, V> map;

    protected abstract V load(K k);

    SimpleCache() {
        this.map = new HashMap();
    }

    final V get(K key) {
        if (this.map.containsKey(key)) {
            return this.map.get(key);
        }
        V value = load(key);
        this.map.put(key, value);
        return value;
    }
}
