package android.arch.core.internal;

import android.arch.core.internal.SafeIterableMap;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import java.util.HashMap;
import java.util.Map;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class FastSafeIterableMap<K, V> extends SafeIterableMap<K, V> {
    private HashMap<K, SafeIterableMap.Entry<K, V>> mHashMap = new HashMap<>();

    /* access modifiers changed from: protected */
    public SafeIterableMap.Entry<K, V> get(K k) {
        return this.mHashMap.get(k);
    }

    public V putIfAbsent(@NonNull K key, @NonNull V v) {
        SafeIterableMap.Entry<K, V> current = get(key);
        if (current != null) {
            return current.mValue;
        }
        this.mHashMap.put(key, put(key, v));
        return null;
    }

    public V remove(@NonNull K key) {
        V removed = super.remove(key);
        this.mHashMap.remove(key);
        return removed;
    }

    public boolean contains(K key) {
        return this.mHashMap.containsKey(key);
    }

    public Map.Entry<K, V> ceil(K k) {
        if (contains(k)) {
            return this.mHashMap.get(k).mPrevious;
        }
        return null;
    }
}
