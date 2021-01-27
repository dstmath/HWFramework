package android.test;

import java.util.HashMap;
import java.util.Map;

/* access modifiers changed from: package-private */
@Deprecated
public abstract class SimpleCache<K, V> {
    private Map<K, V> map = new HashMap();

    /* access modifiers changed from: protected */
    public abstract V load(K k);

    SimpleCache() {
    }

    /* access modifiers changed from: package-private */
    public final V get(K key) {
        if (this.map.containsKey(key)) {
            return this.map.get(key);
        }
        V value = load(key);
        this.map.put(key, value);
        return value;
    }
}
