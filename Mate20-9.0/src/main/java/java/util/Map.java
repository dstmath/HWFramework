package java.util;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Map<K, V> {

    public interface Entry<K, V> {
        boolean equals(Object obj);

        K getKey();

        V getValue();

        int hashCode();

        V setValue(V v);

        static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey() {
            return $$Lambda$Map$Entry$zJtjVuaqJl6rzQLvCcTd4dnXnnw.INSTANCE;
        }

        static <K, V extends Comparable<? super V>> Comparator<Entry<K, V>> comparingByValue() {
            return $$Lambda$Map$Entry$acJOHw6hO1wh4v9r2vtUuCFe5vI.INSTANCE;
        }

        static <K, V> Comparator<Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return new Object() {
                public final int compare(Object obj, Object obj2) {
                    return Comparator.this.compare(((Map.Entry) obj).getKey(), ((Map.Entry) obj2).getKey());
                }
            };
        }

        static <K, V> Comparator<Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
            Objects.requireNonNull(cmp);
            return new Object() {
                public final int compare(Object obj, Object obj2) {
                    return Comparator.this.compare(((Map.Entry) obj).getValue(), ((Map.Entry) obj2).getValue());
                }
            };
        }
    }

    void clear();

    boolean containsKey(Object obj);

    boolean containsValue(Object obj);

    Set<Entry<K, V>> entrySet();

    boolean equals(Object obj);

    V get(Object obj);

    int hashCode();

    boolean isEmpty();

    Set<K> keySet();

    V put(K k, V v);

    void putAll(Map<? extends K, ? extends V> map);

    V remove(Object obj);

    int size();

    Collection<V> values();

    V getOrDefault(Object key, V defaultValue) {
        V v = get(key);
        return (v != null || containsKey(key)) ? v : defaultValue;
    }

    void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Entry<K, V> entry : entrySet()) {
            try {
                action.accept(entry.getKey(), entry.getValue());
            } catch (IllegalStateException ise) {
                throw new ConcurrentModificationException((Throwable) ise);
            }
        }
    }

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Entry<K, V> entry : entrySet()) {
            try {
                try {
                    entry.setValue(function.apply(entry.getKey(), entry.getValue()));
                } catch (IllegalStateException ise) {
                    throw new ConcurrentModificationException((Throwable) ise);
                }
            } catch (IllegalStateException ise2) {
                throw new ConcurrentModificationException((Throwable) ise2);
            }
        }
    }

    V putIfAbsent(K key, V value) {
        V v = get(key);
        if (v == null) {
            return put(key, value);
        }
        return v;
    }

    boolean remove(Object key, Object value) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, value) || (curValue == null && !containsKey(key))) {
            return false;
        }
        remove(key);
        return true;
    }

    boolean replace(K key, V oldValue, V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    V replace(K key, V value) {
        V v = get(key);
        V curValue = v;
        if (v != null || containsKey(key)) {
            return put(key, value);
        }
        return curValue;
    }

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v = get(key);
        V v2 = v;
        if (v == null) {
            V apply = mappingFunction.apply(key);
            V newValue = apply;
            if (apply != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return v2;
    }

    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V v = get(key);
        V oldValue = v;
        if (v == null) {
            return null;
        }
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
            put(key, newValue);
            return newValue;
        }
        remove(key);
        return null;
    }

    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = get(key);
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
            put(key, newValue);
            return newValue;
        } else if (oldValue == null && !containsKey(key)) {
            return null;
        } else {
            remove(key);
            return null;
        }
    }

    V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        V newValue = oldValue == null ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }
}
