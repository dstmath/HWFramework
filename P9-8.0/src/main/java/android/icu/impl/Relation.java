package android.icu.impl;

import android.icu.util.Freezable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Relation<K, V> implements Freezable<Relation<K, V>> {
    private Map<K, Set<V>> data;
    volatile boolean frozen;
    Object[] setComparatorParam;
    Constructor<? extends Set<V>> setCreator;

    static class SimpleEntry<K, V> implements Entry<K, V> {
        K key;
        V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Entry<K, V> e) {
            this.key = e.getKey();
            this.value = e.getValue();
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    public static <K, V> Relation<K, V> of(Map<K, Set<V>> map, Class<?> setCreator) {
        return new Relation(map, setCreator);
    }

    public static <K, V> Relation<K, V> of(Map<K, Set<V>> map, Class<?> setCreator, Comparator<V> setComparator) {
        return new Relation(map, setCreator, setComparator);
    }

    public Relation(Map<K, Set<V>> map, Class<?> setCreator) {
        this(map, setCreator, null);
    }

    public Relation(Map<K, Set<V>> map, Class<?> setCreator, Comparator<V> setComparator) {
        Object[] objArr = null;
        this.frozen = false;
        if (setComparator != null) {
            objArr = new Object[]{setComparator};
        }
        try {
            Map map2;
            this.setComparatorParam = objArr;
            if (setComparator == null) {
                this.setCreator = setCreator.getConstructor(new Class[0]);
                this.setCreator.newInstance(this.setComparatorParam);
            } else {
                this.setCreator = setCreator.getConstructor(new Class[]{Comparator.class});
                this.setCreator.newInstance(this.setComparatorParam);
            }
            if (map2 == null) {
                map2 = new HashMap();
            }
            this.data = map2;
        } catch (Exception e) {
            throw ((RuntimeException) new IllegalArgumentException("Can't create new set").initCause(e));
        }
    }

    public void clear() {
        this.data.clear();
    }

    public boolean containsKey(Object key) {
        return this.data.containsKey(key);
    }

    public boolean containsValue(Object value) {
        for (Set<V> values : this.data.values()) {
            if (values.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public final Set<Entry<K, V>> entrySet() {
        return keyValueSet();
    }

    public Set<Entry<K, Set<V>>> keyValuesSet() {
        return this.data.entrySet();
    }

    public Set<Entry<K, V>> keyValueSet() {
        Set<Entry<K, V>> result = new LinkedHashSet();
        for (K key : this.data.keySet()) {
            for (V value : (Set) this.data.get(key)) {
                result.add(new SimpleEntry(key, value));
            }
        }
        return result;
    }

    public boolean equals(Object o) {
        if (o != null && o.getClass() == getClass()) {
            return this.data.equals(((Relation) o).data);
        }
        return false;
    }

    public Set<V> getAll(Object key) {
        return (Set) this.data.get(key);
    }

    public Set<V> get(Object key) {
        return (Set) this.data.get(key);
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public Set<K> keySet() {
        return this.data.keySet();
    }

    public V put(K key, V value) {
        Set<V> set = (Set) this.data.get(key);
        if (set == null) {
            Map map = this.data;
            set = newSet();
            map.put(key, set);
        }
        set.add(value);
        return value;
    }

    public V putAll(K key, Collection<? extends V> values) {
        Set<V> set = (Set) this.data.get(key);
        if (set == null) {
            Map map = this.data;
            set = newSet();
            map.put(key, set);
        }
        set.addAll(values);
        if (values.size() == 0) {
            return null;
        }
        return values.iterator().next();
    }

    public V putAll(Collection<K> keys, V value) {
        V result = null;
        for (K key : keys) {
            result = put(key, value);
        }
        return result;
    }

    private Set<V> newSet() {
        try {
            return (Set) this.setCreator.newInstance(this.setComparatorParam);
        } catch (Exception e) {
            throw ((RuntimeException) new IllegalArgumentException("Can't create new set").initCause(e));
        }
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        for (Entry<? extends K, ? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void putAll(Relation<? extends K, ? extends V> t) {
        for (K key : t.keySet()) {
            for (V value : t.getAll(key)) {
                put(key, value);
            }
        }
    }

    public Set<V> removeAll(K key) {
        try {
            return (Set) this.data.remove(key);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public boolean remove(K key, V value) {
        try {
            Set<V> set = (Set) this.data.get(key);
            if (set == null) {
                return false;
            }
            boolean result = set.remove(value);
            if (set.size() == 0) {
                this.data.remove(key);
            }
            return result;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public int size() {
        return this.data.size();
    }

    public Set<V> values() {
        return (Set) values(new LinkedHashSet());
    }

    public <C extends Collection<V>> C values(C result) {
        for (Entry<K, Set<V>> keyValue : this.data.entrySet()) {
            result.addAll((Collection) keyValue.getValue());
        }
        return result;
    }

    public String toString() {
        return this.data.toString();
    }

    public Relation<K, V> addAllInverted(Relation<V, K> source) {
        for (V value : source.data.keySet()) {
            for (K key : (Set) source.data.get(value)) {
                put(key, value);
            }
        }
        return this;
    }

    public Relation<K, V> addAllInverted(Map<V, K> source) {
        for (Entry<V, K> entry : source.entrySet()) {
            put(entry.getValue(), entry.getKey());
        }
        return this;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public Relation<K, V> freeze() {
        if (!this.frozen) {
            for (K key : this.data.keySet()) {
                this.data.put(key, Collections.unmodifiableSet((Set) this.data.get(key)));
            }
            this.data = Collections.unmodifiableMap(this.data);
            this.frozen = true;
        }
        return this;
    }

    public Relation<K, V> cloneAsThawed() {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Relation<K, V> toBeRemoved) {
        boolean result = false;
        for (K key : toBeRemoved.keySet()) {
            try {
                Set<V> values = toBeRemoved.getAll(key);
                if (values != null) {
                    result |= removeAll(key, values);
                }
            } catch (NullPointerException e) {
            }
        }
        return result;
    }

    public Set<V> removeAll(K... keys) {
        return removeAll(Arrays.asList(keys));
    }

    public boolean removeAll(K key, Iterable<V> toBeRemoved) {
        boolean result = false;
        for (V value : toBeRemoved) {
            result |= remove(key, value);
        }
        return result;
    }

    public Set<V> removeAll(Collection<K> toBeRemoved) {
        Set<V> result = new LinkedHashSet();
        for (K key : toBeRemoved) {
            try {
                Set<V> removals = (Set) this.data.remove(key);
                if (removals != null) {
                    result.addAll(removals);
                }
            } catch (NullPointerException e) {
            }
        }
        return result;
    }
}
