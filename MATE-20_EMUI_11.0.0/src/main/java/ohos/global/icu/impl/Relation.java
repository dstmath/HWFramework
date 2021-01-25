package ohos.global.icu.impl;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import ohos.global.icu.util.Freezable;

public class Relation<K, V> implements Freezable<Relation<K, V>> {
    private Map<K, Set<V>> data;
    volatile boolean frozen;
    Object[] setComparatorParam;
    Constructor<? extends Set<V>> setCreator;

    public static <K, V> Relation<K, V> of(Map<K, Set<V>> map, Class<?> cls) {
        return new Relation<>(map, cls);
    }

    public static <K, V> Relation<K, V> of(Map<K, Set<V>> map, Class<?> cls, Comparator<V> comparator) {
        return new Relation<>(map, cls, comparator);
    }

    public Relation(Map<K, Set<V>> map, Class<?> cls) {
        this(map, cls, null);
    }

    public Relation(Map<K, Set<V>> map, Class<?> cls, Comparator<V> comparator) {
        Object[] objArr;
        this.frozen = false;
        if (comparator == null) {
            objArr = null;
        } else {
            try {
                objArr = new Object[]{comparator};
            } catch (Exception e) {
                throw ((RuntimeException) new IllegalArgumentException("Can't create new set").initCause(e));
            }
        }
        this.setComparatorParam = objArr;
        if (comparator == null) {
            this.setCreator = (Constructor<? extends Set<V>>) cls.getConstructor(new Class[0]);
            this.setCreator.newInstance(this.setComparatorParam);
        } else {
            this.setCreator = (Constructor<? extends Set<V>>) cls.getConstructor(Comparator.class);
            this.setCreator.newInstance(this.setComparatorParam);
        }
        this.data = map == null ? new HashMap<>() : map;
    }

    public void clear() {
        this.data.clear();
    }

    public boolean containsKey(Object obj) {
        return this.data.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        for (Set<V> set : this.data.values()) {
            if (set.contains(obj)) {
                return true;
            }
        }
        return false;
    }

    public final Set<Map.Entry<K, V>> entrySet() {
        return keyValueSet();
    }

    public Set<Map.Entry<K, Set<V>>> keyValuesSet() {
        return this.data.entrySet();
    }

    public Set<Map.Entry<K, V>> keyValueSet() {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        for (K k : this.data.keySet()) {
            for (V v : this.data.get(k)) {
                linkedHashSet.add(new SimpleEntry(k, v));
            }
        }
        return linkedHashSet;
    }

    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == getClass()) {
            return this.data.equals(((Relation) obj).data);
        }
        return false;
    }

    public Set<V> getAll(Object obj) {
        return this.data.get(obj);
    }

    public Set<V> get(Object obj) {
        return this.data.get(obj);
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

    public V put(K k, V v) {
        Set<V> set;
        Set<V> set2 = this.data.get(k);
        if (set2 == null) {
            Map<K, Set<V>> map = this.data;
            set = newSet();
            map.put(k, set);
        } else {
            set = set2;
        }
        set.add(v);
        return v;
    }

    public V putAll(K k, Collection<? extends V> collection) {
        Set<V> set;
        Set<V> set2 = this.data.get(k);
        if (set2 == null) {
            Map<K, Set<V>> map = this.data;
            set = newSet();
            map.put(k, set);
        } else {
            set = set2;
        }
        set.addAll(collection);
        if (collection.size() == 0) {
            return null;
        }
        return (V) collection.iterator().next();
    }

    public V putAll(Collection<K> collection, V v) {
        V v2 = null;
        for (K k : collection) {
            v2 = put(k, v);
        }
        return v2;
    }

    private Set<V> newSet() {
        try {
            return (Set) this.setCreator.newInstance(this.setComparatorParam);
        } catch (Exception e) {
            throw ((RuntimeException) new IllegalArgumentException("Can't create new set").initCause(e));
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: ohos.global.icu.impl.Relation<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: ohos.global.icu.impl.Relation<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public void putAll(Relation<? extends K, ? extends V> relation) {
        for (Object obj : relation.keySet()) {
            Iterator<? extends V> it = relation.getAll(obj).iterator();
            while (it.hasNext()) {
                put(obj, it.next());
            }
        }
    }

    public Set<V> removeAll(K k) {
        try {
            return this.data.remove(k);
        } catch (NullPointerException unused) {
            return null;
        }
    }

    public boolean remove(K k, V v) {
        try {
            Set<V> set = this.data.get(k);
            if (set == null) {
                return false;
            }
            boolean remove = set.remove(v);
            if (set.size() == 0) {
                this.data.remove(k);
            }
            return remove;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    public int size() {
        return this.data.size();
    }

    public Set<V> values() {
        return (Set) values(new LinkedHashSet());
    }

    public <C extends Collection<V>> C values(C c) {
        for (Map.Entry<K, Set<V>> entry : this.data.entrySet()) {
            c.addAll(entry.getValue());
        }
        return c;
    }

    public String toString() {
        return this.data.toString();
    }

    /* access modifiers changed from: package-private */
    public static class SimpleEntry<K, V> implements Map.Entry<K, V> {
        K key;
        V value;

        public SimpleEntry(K k, V v) {
            this.key = k;
            this.value = v;
        }

        public SimpleEntry(Map.Entry<K, V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public V setValue(V v) {
            V v2 = this.value;
            this.value = v;
            return v2;
        }
    }

    public Relation<K, V> addAllInverted(Relation<V, K> relation) {
        for (K k : relation.data.keySet()) {
            for (V v : relation.data.get(k)) {
                put(v, k);
            }
        }
        return this;
    }

    public Relation<K, V> addAllInverted(Map<V, K> map) {
        for (Map.Entry<V, K> entry : map.entrySet()) {
            put(entry.getValue(), entry.getKey());
        }
        return this;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public Relation<K, V> freeze() {
        if (!this.frozen) {
            for (K k : this.data.keySet()) {
                Map<K, Set<V>> map = this.data;
                map.put(k, Collections.unmodifiableSet(map.get(k)));
            }
            this.data = Collections.unmodifiableMap(this.data);
            this.frozen = true;
        }
        return this;
    }

    public Relation<K, V> cloneAsThawed() {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Relation<K, V> relation) {
        boolean z = false;
        for (K k : relation.keySet()) {
            try {
                Set<V> all = relation.getAll(k);
                if (all != null) {
                    z |= removeAll(k, all);
                }
            } catch (NullPointerException unused) {
            }
        }
        return z;
    }

    @SafeVarargs
    public final Set<V> removeAll(K... kArr) {
        return removeAll((Collection) Arrays.asList(kArr));
    }

    public boolean removeAll(K k, Iterable<V> iterable) {
        boolean z = false;
        for (V v : iterable) {
            z |= remove(k, v);
        }
        return z;
    }

    public Set<V> removeAll(Collection<K> collection) {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        for (K k : collection) {
            try {
                Set<V> remove = this.data.remove(k);
                if (remove != null) {
                    linkedHashSet.addAll(remove);
                }
            } catch (NullPointerException unused) {
            }
        }
        return linkedHashSet;
    }
}
