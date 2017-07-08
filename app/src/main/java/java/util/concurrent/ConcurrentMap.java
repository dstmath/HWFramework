package java.util.concurrent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ConcurrentMap<K, V> extends Map<K, V> {

    final /* synthetic */ class -void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ BiFunction val$function;
        private /* synthetic */ ConcurrentMap val$this;

        public /* synthetic */ -void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0(ConcurrentMap concurrentMap, BiFunction biFunction) {
            this.val$this = concurrentMap;
            this.val$function = biFunction;
        }

        public void accept(Object arg0, Object arg1) {
            this.val$this.-java_util_concurrent_ConcurrentMap_lambda$1(this.val$function, arg0, arg1);
        }
    }

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
        forEach(new -void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0(this, function));
    }

    /* synthetic */ void -java_util_concurrent_ConcurrentMap_lambda$1(BiFunction function, Object k, Object v) {
        while (!replace(k, v, function.apply(k, v))) {
            v = get(k);
            if (v == null) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        while (true) {
            V oldValue = get(key);
            while (true) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue == null) {
                    break;
                } else if (oldValue != null) {
                    break;
                } else {
                    oldValue = putIfAbsent(key, newValue);
                    if (oldValue == null) {
                        return newValue;
                    }
                }
            }
            if (replace(key, oldValue, newValue)) {
                return newValue;
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
