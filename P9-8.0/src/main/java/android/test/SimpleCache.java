package android.test;

import java.util.HashMap;
import java.util.Map;

@Deprecated
abstract class SimpleCache<K, V> {
    private Map<K, V> map = new HashMap();

    protected abstract V load(K k);

    SimpleCache() {
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
