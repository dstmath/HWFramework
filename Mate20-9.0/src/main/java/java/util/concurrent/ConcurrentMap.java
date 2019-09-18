package java.util.concurrent;

import java.util.Map;
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
        for (Map.Entry<K, V> entry : entrySet()) {
            try {
                action.accept(entry.getKey(), entry.getValue());
            } catch (IllegalStateException e) {
            }
        }
    }

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        forEach(new BiConsumer(function) {
            private final /* synthetic */ BiFunction f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj, Object obj2) {
                ConcurrentMap.lambda$replaceAll$0(ConcurrentMap.this, this.f$1, obj, obj2);
            }
        });
    }

    static /* synthetic */ void lambda$replaceAll$0(ConcurrentMap concurrentMap, BiFunction function, Object k, Object v) {
        while (!concurrentMap.replace(k, v, function.apply(k, v))) {
            Object obj = concurrentMap.get(k);
            v = obj;
            if (obj == null) {
                return;
            }
        }
    }

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v = get(key);
        V oldValue = v;
        if (v == null) {
            V apply = mappingFunction.apply(key);
            V newValue = apply;
            if (apply != null) {
                V putIfAbsent = putIfAbsent(key, newValue);
                oldValue = putIfAbsent;
                if (putIfAbsent == null) {
                    return newValue;
                }
            }
        }
        V newValue2 = oldValue;
        V oldValue2 = newValue2;
        return newValue2;
    }

    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V newValue;
        Objects.requireNonNull(remappingFunction);
        while (true) {
            V v = get(key);
            V oldValue = v;
            if (v == null) {
                return null;
            }
            newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                if (remove(key, oldValue)) {
                    break;
                }
            } else if (replace(key, oldValue, newValue)) {
                break;
            }
        }
        return newValue;
    }

    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        while (true) {
            V oldValue = get(key);
            while (true) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    if (oldValue == null) {
                        V putIfAbsent = putIfAbsent(key, newValue);
                        oldValue = putIfAbsent;
                        if (putIfAbsent == null) {
                            return newValue;
                        }
                    } else if (replace(key, oldValue, newValue)) {
                        return newValue;
                    }
                } else if (oldValue == null || remove(key, oldValue)) {
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
                V newValue = putIfAbsent(key, value);
                oldValue = newValue;
                if (newValue == null) {
                    return value;
                }
            }
            V newValue2 = remappingFunction.apply(oldValue, value);
            if (newValue2 != null) {
                if (replace(key, oldValue, newValue2)) {
                    return newValue2;
                }
            } else if (remove(key, oldValue)) {
                return null;
            }
        }
    }
}
