package android.support.v4.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

abstract class MapCollections<K, V> {
    MapCollections<K, V>.EntrySet mEntrySet;
    MapCollections<K, V>.KeySet mKeySet;
    MapCollections<K, V>.ValuesCollection mValues;

    final class ArrayIterator<T> implements Iterator<T> {
        boolean mCanRemove = false;
        int mIndex;
        final int mOffset;
        int mSize;

        ArrayIterator(int offset) {
            this.mOffset = offset;
            this.mSize = MapCollections.this.colGetSize();
        }

        public boolean hasNext() {
            return this.mIndex < this.mSize;
        }

        public T next() {
            if (hasNext()) {
                Object res = MapCollections.this.colGetEntry(this.mIndex, this.mOffset);
                this.mIndex++;
                this.mCanRemove = true;
                return res;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.mCanRemove) {
                this.mIndex--;
                this.mSize--;
                this.mCanRemove = false;
                MapCollections.this.colRemoveAt(this.mIndex);
                return;
            }
            throw new IllegalStateException();
        }
    }

    final class EntrySet implements Set<Map.Entry<K, V>> {
        EntrySet() {
        }

        public boolean add(Map.Entry<K, V> entry) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends Map.Entry<K, V>> collection) {
            int oldSize = MapCollections.this.colGetSize();
            for (Map.Entry<K, V> entry : collection) {
                MapCollections.this.colPut(entry.getKey(), entry.getValue());
            }
            return oldSize != MapCollections.this.colGetSize();
        }

        public void clear() {
            MapCollections.this.colClear();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            int index = MapCollections.this.colIndexOfKey(e.getKey());
            if (index < 0) {
                return false;
            }
            return ContainerHelpers.equal(MapCollections.this.colGetEntry(index, 1), e.getValue());
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object contains : collection) {
                if (!contains(contains)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return MapCollections.this.colGetSize() == 0;
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return new MapIterator();
        }

        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return MapCollections.this.colGetSize();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] tArr) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object object) {
            return MapCollections.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int i;
            int result = 0;
            for (int i2 = MapCollections.this.colGetSize() - 1; i2 >= 0; i2--) {
                int i3 = 0;
                Object key = MapCollections.this.colGetEntry(i2, 0);
                Object value = MapCollections.this.colGetEntry(i2, 1);
                if (key == null) {
                    i = 0;
                } else {
                    i = key.hashCode();
                }
                if (value != null) {
                    i3 = value.hashCode();
                }
                result += i3 ^ i;
            }
            return result;
        }
    }

    final class KeySet implements Set<K> {
        KeySet() {
        }

        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            MapCollections.this.colClear();
        }

        public boolean contains(Object object) {
            return MapCollections.this.colIndexOfKey(object) >= 0;
        }

        public boolean containsAll(Collection<?> collection) {
            return MapCollections.containsAllHelper(MapCollections.this.colGetMap(), collection);
        }

        public boolean isEmpty() {
            return MapCollections.this.colGetSize() == 0;
        }

        public Iterator<K> iterator() {
            return new ArrayIterator(0);
        }

        public boolean remove(Object object) {
            int index = MapCollections.this.colIndexOfKey(object);
            if (index < 0) {
                return false;
            }
            MapCollections.this.colRemoveAt(index);
            return true;
        }

        public boolean removeAll(Collection<?> collection) {
            return MapCollections.removeAllHelper(MapCollections.this.colGetMap(), collection);
        }

        public boolean retainAll(Collection<?> collection) {
            return MapCollections.retainAllHelper(MapCollections.this.colGetMap(), collection);
        }

        public int size() {
            return MapCollections.this.colGetSize();
        }

        public Object[] toArray() {
            return MapCollections.this.toArrayHelper(0);
        }

        public <T> T[] toArray(T[] array) {
            return MapCollections.this.toArrayHelper(array, 0);
        }

        public boolean equals(Object object) {
            return MapCollections.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int result = 0;
            for (int i = MapCollections.this.colGetSize() - 1; i >= 0; i--) {
                int i2 = 0;
                Object obj = MapCollections.this.colGetEntry(i, 0);
                if (obj != null) {
                    i2 = obj.hashCode();
                }
                result += i2;
            }
            return result;
        }
    }

    final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
        int mEnd;
        boolean mEntryValid = false;
        int mIndex;

        MapIterator() {
            this.mEnd = this$0.colGetSize() - 1;
            this.mIndex = -1;
        }

        public boolean hasNext() {
            return this.mIndex < this.mEnd;
        }

        public Map.Entry<K, V> next() {
            if (hasNext()) {
                this.mIndex++;
                this.mEntryValid = true;
                return this;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.mEntryValid) {
                MapCollections.this.colRemoveAt(this.mIndex);
                this.mIndex--;
                this.mEnd--;
                this.mEntryValid = false;
                return;
            }
            throw new IllegalStateException();
        }

        public K getKey() {
            if (this.mEntryValid) {
                return MapCollections.this.colGetEntry(this.mIndex, 0);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public V getValue() {
            if (this.mEntryValid) {
                return MapCollections.this.colGetEntry(this.mIndex, 1);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public V setValue(V object) {
            if (this.mEntryValid) {
                return MapCollections.this.colSetValue(this.mIndex, object);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public boolean equals(Object o) {
            if (this.mEntryValid) {
                boolean z = false;
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<?, ?> e = (Map.Entry) o;
                if (ContainerHelpers.equal(e.getKey(), MapCollections.this.colGetEntry(this.mIndex, 0)) && ContainerHelpers.equal(e.getValue(), MapCollections.this.colGetEntry(this.mIndex, 1))) {
                    z = true;
                }
                return z;
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public int hashCode() {
            int i;
            if (this.mEntryValid) {
                int i2 = 0;
                Object key = MapCollections.this.colGetEntry(this.mIndex, 0);
                Object value = MapCollections.this.colGetEntry(this.mIndex, 1);
                if (key == null) {
                    i = 0;
                } else {
                    i = key.hashCode();
                }
                if (value != null) {
                    i2 = value.hashCode();
                }
                return i2 ^ i;
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    final class ValuesCollection implements Collection<V> {
        ValuesCollection() {
        }

        public boolean add(V v) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            MapCollections.this.colClear();
        }

        public boolean contains(Object object) {
            return MapCollections.this.colIndexOfValue(object) >= 0;
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object contains : collection) {
                if (!contains(contains)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return MapCollections.this.colGetSize() == 0;
        }

        public Iterator<V> iterator() {
            return new ArrayIterator(1);
        }

        public boolean remove(Object object) {
            int index = MapCollections.this.colIndexOfValue(object);
            if (index < 0) {
                return false;
            }
            MapCollections.this.colRemoveAt(index);
            return true;
        }

        public boolean removeAll(Collection<?> collection) {
            int N = MapCollections.this.colGetSize();
            boolean changed = false;
            int i = 0;
            while (i < N) {
                if (collection.contains(MapCollections.this.colGetEntry(i, 1))) {
                    MapCollections.this.colRemoveAt(i);
                    i--;
                    N--;
                    changed = true;
                }
                i++;
            }
            return changed;
        }

        public boolean retainAll(Collection<?> collection) {
            int N = MapCollections.this.colGetSize();
            boolean changed = false;
            int i = 0;
            while (i < N) {
                if (!collection.contains(MapCollections.this.colGetEntry(i, 1))) {
                    MapCollections.this.colRemoveAt(i);
                    i--;
                    N--;
                    changed = true;
                }
                i++;
            }
            return changed;
        }

        public int size() {
            return MapCollections.this.colGetSize();
        }

        public Object[] toArray() {
            return MapCollections.this.toArrayHelper(1);
        }

        public <T> T[] toArray(T[] array) {
            return MapCollections.this.toArrayHelper(array, 1);
        }
    }

    /* access modifiers changed from: protected */
    public abstract void colClear();

    /* access modifiers changed from: protected */
    public abstract Object colGetEntry(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract Map<K, V> colGetMap();

    /* access modifiers changed from: protected */
    public abstract int colGetSize();

    /* access modifiers changed from: protected */
    public abstract int colIndexOfKey(Object obj);

    /* access modifiers changed from: protected */
    public abstract int colIndexOfValue(Object obj);

    /* access modifiers changed from: protected */
    public abstract void colPut(K k, V v);

    /* access modifiers changed from: protected */
    public abstract void colRemoveAt(int i);

    /* access modifiers changed from: protected */
    public abstract V colSetValue(int i, V v);

    MapCollections() {
    }

    public static <K, V> boolean containsAllHelper(Map<K, V> map, Collection<?> collection) {
        for (Object containsKey : collection) {
            if (!map.containsKey(containsKey)) {
                return false;
            }
        }
        return true;
    }

    public static <K, V> boolean removeAllHelper(Map<K, V> map, Collection<?> collection) {
        int oldSize = map.size();
        for (Object remove : collection) {
            map.remove(remove);
        }
        return oldSize != map.size();
    }

    public static <K, V> boolean retainAllHelper(Map<K, V> map, Collection<?> collection) {
        int oldSize = map.size();
        Iterator<K> it = map.keySet().iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
            }
        }
        return oldSize != map.size();
    }

    public Object[] toArrayHelper(int offset) {
        int N = colGetSize();
        Object[] result = new Object[N];
        for (int i = 0; i < N; i++) {
            result[i] = colGetEntry(i, offset);
        }
        return result;
    }

    public <T> T[] toArrayHelper(T[] array, int offset) {
        int N = colGetSize();
        if (array.length < N) {
            array = (Object[]) Array.newInstance(array.getClass().getComponentType(), N);
        }
        for (int i = 0; i < N; i++) {
            array[i] = colGetEntry(i, offset);
        }
        if (array.length > N) {
            array[N] = null;
        }
        return array;
    }

    public static <T> boolean equalsSetHelper(Set<T> set, Object object) {
        boolean z = true;
        if (set == object) {
            return true;
        }
        if (!(object instanceof Set)) {
            return false;
        }
        Set<?> s = (Set) object;
        try {
            if (set.size() != s.size() || !set.containsAll(s)) {
                z = false;
            }
            return z;
        } catch (NullPointerException e) {
            return false;
        } catch (ClassCastException e2) {
            return false;
        }
    }

    public Set<Map.Entry<K, V>> getEntrySet() {
        if (this.mEntrySet == null) {
            this.mEntrySet = new EntrySet();
        }
        return this.mEntrySet;
    }

    public Set<K> getKeySet() {
        if (this.mKeySet == null) {
            this.mKeySet = new KeySet();
        }
        return this.mKeySet;
    }

    public Collection<V> getValues() {
        if (this.mValues == null) {
            this.mValues = new ValuesCollection();
        }
        return this.mValues;
    }
}
