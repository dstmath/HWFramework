package java.util.concurrent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ConcurrentMap<K, V> extends Map<K, V> {
    V putIfAbsent(K k, V v);

    boolean remove(Object obj, Object obj2);

    V replace(K k, V v);

    boolean replace(K k, V v, V v2);

    V getOrDefault(Object key, V defaultValue) {
        V v = get(key);
        return v != null ? v : defaultValue;
    }

    void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Entry<K, V> entry : entrySet()) {
            try {
                action.accept(entry.getKey(), entry.getValue());
            } catch (IllegalStateException e) {
            }
        }
    }

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        forEach(new -$Lambda$xR9BLpu6SifNikvFgr4lEiECBsk(this, function));
    }

    /* synthetic */ void lambda$-java_util_concurrent_ConcurrentMap_11766(BiFunction function, Object k, Object v) {
        V v2;
        while (!replace(k, v2, function.apply(k, v2))) {
            v2 = get(k);
            if (v2 == null) {
                return;
            }
        }
    }

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V oldValue = get(key);
        if (oldValue == null) {
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                oldValue = putIfAbsent(key, newValue);
                if (oldValue == null) {
                    return newValue;
                }
            }
        }
        return oldValue;
    }

    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V newValue;
        Objects.requireNonNull(remappingFunction);
        boolean remove;
        do {
            V oldValue = get(key);
            if (oldValue == null) {
                return null;
            }
            newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                remove = remove(key, oldValue);
                continue;
            } else {
                remove = replace(key, oldValue, newValue);
                continue;
            }
        } while (!remove);
        return newValue;
    }

    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        while (true) {
            V oldValue = get(key);
            while (true) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    if (oldValue == null) {
                        oldValue = putIfAbsent(key, newValue);
                        if (oldValue == null) {
                            return newValue;
                        }
                    } else if (replace(key, oldValue, newValue)) {
                        return newValue;
                    }
                } else if (oldValue == null || remove(key, oldValue)) {
                    return null;
                }
            }
        }
        return null;
    }

    V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        while (true) {
            V oldValue = get(key);
            while (oldValue == null) {
                oldValue = putIfAbsent(key, value);
                if (oldValue == null) {
                    return value;
                }
            }
            V newValue = remappingFunction.apply(oldValue, value);
            if (newValue != null) {
                if (replace(key, oldValue, newValue)) {
                    return newValue;
                }
            } else if (remove(key, oldValue)) {
                return null;
            }
        }
    }
}
