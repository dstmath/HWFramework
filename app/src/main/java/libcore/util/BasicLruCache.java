package libcore.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BasicLruCache<K, V> {
    private final LinkedHashMap<K, V> map;
    private final int maxSize;

    public BasicLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap(0, 0.75f, true);
    }

    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            V result = this.map.get(key);
            if (result != null) {
                return result;
            }
            result = create(key);
            synchronized (this) {
                if (result != null) {
                    this.map.put(key, result);
                    trimToSize(this.maxSize);
                }
            }
            return result;
        }
    }

    public final synchronized V put(K key, V value) {
        V previous;
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        } else {
            previous = this.map.put(key, value);
            trimToSize(this.maxSize);
        }
        return previous;
    }

    private void trimToSize(int maxSize) {
        while (this.map.size() > maxSize) {
            Entry<K, V> toEvict = this.map.eldest();
            K key = toEvict.getKey();
            V value = toEvict.getValue();
            this.map.remove(key);
            entryEvicted(key, value);
        }
    }

    protected void entryEvicted(K k, V v) {
    }

    protected V create(K k) {
        return null;
    }

    public final synchronized Map<K, V> snapshot() {
        return new LinkedHashMap(this.map);
    }

    public final synchronized void evictAll() {
        trimToSize(0);
    }
}
