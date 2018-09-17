package java.util;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Map<K, V> {

    public interface Entry<K, V> {

        final /* synthetic */ class -java_util_Comparator_comparingByKey__LambdaImpl0 implements Comparator, Serializable {
            public int compare(Object arg0, Object arg1) {
                return ((Comparable) ((Entry) arg0).getKey()).compareTo(((Entry) arg1).getKey());
            }
        }

        final /* synthetic */ class -java_util_Comparator_comparingByKey_java_util_Comparator_cmp_LambdaImpl0 implements Comparator, Serializable {
            private /* synthetic */ Comparator val$cmp;

            public /* synthetic */ -java_util_Comparator_comparingByKey_java_util_Comparator_cmp_LambdaImpl0(Comparator comparator) {
                this.val$cmp = comparator;
            }

            public int compare(Object arg0, Object arg1) {
                return this.val$cmp.compare(((Entry) arg0).getKey(), ((Entry) arg1).getKey());
            }
        }

        final /* synthetic */ class -java_util_Comparator_comparingByValue__LambdaImpl0 implements Comparator, Serializable {
            public int compare(Object arg0, Object arg1) {
                return ((Comparable) ((Entry) arg0).getValue()).compareTo(((Entry) arg1).getValue());
            }
        }

        final /* synthetic */ class -java_util_Comparator_comparingByValue_java_util_Comparator_cmp_LambdaImpl0 implements Comparator, Serializable {
            private /* synthetic */ Comparator val$cmp;

            public /* synthetic */ -java_util_Comparator_comparingByValue_java_util_Comparator_cmp_LambdaImpl0(Comparator comparator) {
                this.val$cmp = comparator;
            }

            public int compare(Object arg0, Object arg1) {
                return this.val$cmp.compare(((Entry) arg0).getValue(), ((Entry) arg1).getValue());
            }
        }

        boolean equals(Object obj);

        K getKey();

        V getValue();

        int hashCode();

        V setValue(V v);

        static <K extends Comparable<? super K>, V> Comparator<Entry<K, V>> comparingByKey() {
            return new -java_util_Comparator_comparingByKey__LambdaImpl0();
        }

        static <K, V extends Comparable<? super V>> Comparator<Entry<K, V>> comparingByValue() {
            return new -java_util_Comparator_comparingByValue__LambdaImpl0();
        }

        static <K, V> Comparator<Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return new -java_util_Comparator_comparingByKey_java_util_Comparator_cmp_LambdaImpl0(cmp);
        }

        static <K, V> Comparator<Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
            Objects.requireNonNull(cmp);
            return new -java_util_Comparator_comparingByValue_java_util_Comparator_cmp_LambdaImpl0(cmp);
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
            } catch (Throwable ise) {
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Entry<K, V> entry : entrySet()) {
            try {
                try {
                    entry.setValue(function.apply(entry.getKey(), entry.getValue()));
                } catch (Throwable ise) {
                    throw new ConcurrentModificationException(ise);
                }
            } catch (Throwable ise2) {
                throw new ConcurrentModificationException(ise2);
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
        V curValue = get(key);
        if (curValue != null || containsKey(key)) {
            return put(key, value);
        }
        return curValue;
    }

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v = get(key);
        if (v == null) {
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return v;
    }

    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = get(key);
        if (oldValue == null) {
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
        V newValue;
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        if (oldValue == null) {
            newValue = value;
        } else {
            newValue = remappingFunction.apply(oldValue, value);
        }
        if (newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }
}
