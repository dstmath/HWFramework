package com.android.framework.protobuf;

import com.android.framework.protobuf.Internal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class MapFieldLite<K, V> implements MutabilityOracle {
    private static final MapFieldLite EMPTY_MAP_FIELD = new MapFieldLite(Collections.emptyMap());
    private boolean isMutable = true;
    private MutatabilityAwareMap<K, V> mapData;

    private MapFieldLite(Map<K, V> mapData2) {
        this.mapData = new MutatabilityAwareMap<>(this, mapData2);
    }

    static {
        EMPTY_MAP_FIELD.makeImmutable();
    }

    public static <K, V> MapFieldLite<K, V> emptyMapField() {
        return EMPTY_MAP_FIELD;
    }

    public static <K, V> MapFieldLite<K, V> newMapField() {
        return new MapFieldLite<>(new LinkedHashMap());
    }

    public Map<K, V> getMap() {
        return Collections.unmodifiableMap(this.mapData);
    }

    public Map<K, V> getMutableMap() {
        return this.mapData;
    }

    public void mergeFrom(MapFieldLite<K, V> other) {
        this.mapData.putAll(copy((Map) other.mapData));
    }

    public void clear() {
        this.mapData.clear();
    }

    private static boolean equals(Object a, Object b) {
        if (!(a instanceof byte[]) || !(b instanceof byte[])) {
            return a.equals(b);
        }
        return Arrays.equals((byte[]) a, (byte[]) b);
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x001e  */
    static <K, V> boolean equals(Map<K, V> a, Map<K, V> b) {
        if (a == b) {
            return true;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (Map.Entry<K, V> entry : a.entrySet()) {
            if (!b.containsKey(entry.getKey()) || !equals((Object) entry.getValue(), (Object) b.get(entry.getKey()))) {
                return false;
            }
            while (r1.hasNext()) {
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        if (!(object instanceof MapFieldLite)) {
            return false;
        }
        return equals((Map) this.mapData, (Map) ((MapFieldLite) object).mapData);
    }

    private static int calculateHashCodeForObject(Object a) {
        if (a instanceof byte[]) {
            return Internal.hashCode((byte[]) a);
        }
        if (!(a instanceof Internal.EnumLite)) {
            return a.hashCode();
        }
        throw new UnsupportedOperationException();
    }

    static <K, V> int calculateHashCodeForMap(Map<K, V> a) {
        int result = 0;
        for (Map.Entry<K, V> entry : a.entrySet()) {
            result += calculateHashCodeForObject(entry.getKey()) ^ calculateHashCodeForObject(entry.getValue());
        }
        return result;
    }

    public int hashCode() {
        return calculateHashCodeForMap(this.mapData);
    }

    private static Object copy(Object object) {
        if (!(object instanceof byte[])) {
            return object;
        }
        byte[] data = (byte[]) object;
        return Arrays.copyOf(data, data.length);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.LinkedHashMap */
    /* JADX WARN: Multi-variable type inference failed */
    static <K, V> Map<K, V> copy(Map<K, V> map) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            linkedHashMap.put(entry.getKey(), copy((Object) entry.getValue()));
        }
        return linkedHashMap;
    }

    public MapFieldLite<K, V> copy() {
        return new MapFieldLite<>(copy((Map) this.mapData));
    }

    public void makeImmutable() {
        this.isMutable = false;
    }

    public boolean isMutable() {
        return this.isMutable;
    }

    @Override // com.android.framework.protobuf.MutabilityOracle
    public void ensureMutable() {
        if (!isMutable()) {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public static class MutatabilityAwareMap<K, V> implements Map<K, V> {
        private final Map<K, V> delegate;
        private final MutabilityOracle mutabilityOracle;

        MutatabilityAwareMap(MutabilityOracle mutabilityOracle2, Map<K, V> delegate2) {
            this.mutabilityOracle = mutabilityOracle2;
            this.delegate = delegate2;
        }

        public int size() {
            return this.delegate.size();
        }

        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        public boolean containsKey(Object key) {
            return this.delegate.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return this.delegate.containsValue(value);
        }

        @Override // java.util.Map
        public V get(Object key) {
            return this.delegate.get(key);
        }

        @Override // java.util.Map
        public V put(K key, V value) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.put(key, value);
        }

        @Override // java.util.Map
        public V remove(Object key) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.remove(key);
        }

        @Override // java.util.Map
        public void putAll(Map<? extends K, ? extends V> m) {
            this.mutabilityOracle.ensureMutable();
            this.delegate.putAll(m);
        }

        public void clear() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.clear();
        }

        @Override // java.util.Map
        public Set<K> keySet() {
            return new MutatabilityAwareSet(this.mutabilityOracle, this.delegate.keySet());
        }

        @Override // java.util.Map
        public Collection<V> values() {
            return new MutatabilityAwareCollection(this.mutabilityOracle, this.delegate.values());
        }

        @Override // java.util.Map
        public Set<Map.Entry<K, V>> entrySet() {
            return new MutatabilityAwareSet(this.mutabilityOracle, this.delegate.entrySet());
        }

        public boolean equals(Object o) {
            return this.delegate.equals(o);
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public String toString() {
            return this.delegate.toString();
        }
    }

    private static class MutatabilityAwareCollection<E> implements Collection<E> {
        private final Collection<E> delegate;
        private final MutabilityOracle mutabilityOracle;

        MutatabilityAwareCollection(MutabilityOracle mutabilityOracle2, Collection<E> delegate2) {
            this.mutabilityOracle = mutabilityOracle2;
            this.delegate = delegate2;
        }

        public int size() {
            return this.delegate.size();
        }

        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        public boolean contains(Object o) {
            return this.delegate.contains(o);
        }

        @Override // java.util.Collection, java.lang.Iterable
        public Iterator<E> iterator() {
            return new MutatabilityAwareIterator(this.mutabilityOracle, this.delegate.iterator());
        }

        public Object[] toArray() {
            return this.delegate.toArray();
        }

        @Override // java.util.Collection
        public <T> T[] toArray(T[] a) {
            return (T[]) this.delegate.toArray(a);
        }

        @Override // java.util.Collection
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.remove(o);
        }

        @Override // java.util.Collection
        public boolean containsAll(Collection<?> c) {
            return this.delegate.containsAll(c);
        }

        @Override // java.util.Collection
        public boolean addAll(Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection
        public boolean removeAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.removeAll(c);
        }

        @Override // java.util.Collection
        public boolean retainAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.retainAll(c);
        }

        public void clear() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.clear();
        }

        public boolean equals(Object o) {
            return this.delegate.equals(o);
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public String toString() {
            return this.delegate.toString();
        }
    }

    private static class MutatabilityAwareSet<E> implements Set<E> {
        private final Set<E> delegate;
        private final MutabilityOracle mutabilityOracle;

        MutatabilityAwareSet(MutabilityOracle mutabilityOracle2, Set<E> delegate2) {
            this.mutabilityOracle = mutabilityOracle2;
            this.delegate = delegate2;
        }

        public int size() {
            return this.delegate.size();
        }

        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        public boolean contains(Object o) {
            return this.delegate.contains(o);
        }

        @Override // java.util.Collection, java.util.Set, java.lang.Iterable
        public Iterator<E> iterator() {
            return new MutatabilityAwareIterator(this.mutabilityOracle, this.delegate.iterator());
        }

        public Object[] toArray() {
            return this.delegate.toArray();
        }

        @Override // java.util.Collection, java.util.Set
        public <T> T[] toArray(T[] a) {
            return (T[]) this.delegate.toArray(a);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean add(E e) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.add(e);
        }

        public boolean remove(Object o) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.remove(o);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean containsAll(Collection<?> c) {
            return this.delegate.containsAll(c);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean addAll(Collection<? extends E> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.addAll(c);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean retainAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.retainAll(c);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean removeAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.removeAll(c);
        }

        public void clear() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.clear();
        }

        public boolean equals(Object o) {
            return this.delegate.equals(o);
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public String toString() {
            return this.delegate.toString();
        }
    }

    private static class MutatabilityAwareIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;
        private final MutabilityOracle mutabilityOracle;

        MutatabilityAwareIterator(MutabilityOracle mutabilityOracle2, Iterator<E> delegate2) {
            this.mutabilityOracle = mutabilityOracle2;
            this.delegate = delegate2;
        }

        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override // java.util.Iterator
        public E next() {
            return this.delegate.next();
        }

        public void remove() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.remove();
        }

        public boolean equals(Object obj) {
            return this.delegate.equals(obj);
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public String toString() {
            return this.delegate.toString();
        }
    }
}
