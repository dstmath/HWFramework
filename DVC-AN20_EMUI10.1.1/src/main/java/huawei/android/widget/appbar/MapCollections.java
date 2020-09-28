package huawei.android.widget.appbar;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/* access modifiers changed from: package-private */
public abstract class MapCollections<K, V> {
    private static final int INVALID_VALUE = -1;
    private static final String MESSAGE = "This container does not support retaining Map.Entry objects";
    MapCollections<K, V>.EntrySet mEntrySet;
    MapCollections<K, V>.KeySet mKeySet;
    MapCollections<K, V>.ValuesCollection mValues;

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

    final class ArrayIterator<T> implements Iterator<T> {
        int mIndex;
        boolean mIsCanRemove = false;
        final int mOffset;
        int mSize;

        ArrayIterator(int offset) {
            this.mOffset = offset;
            this.mSize = MapCollections.this.colGetSize();
        }

        public boolean hasNext() {
            return this.mIndex < this.mSize;
        }

        @Override // java.util.Iterator
        public T next() {
            if (hasNext()) {
                T t = (T) MapCollections.this.colGetEntry(this.mIndex, this.mOffset);
                this.mIndex++;
                this.mIsCanRemove = true;
                return t;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.mIsCanRemove) {
                this.mIndex--;
                this.mSize--;
                this.mIsCanRemove = false;
                MapCollections.this.colRemoveAt(this.mIndex);
                return;
            }
            throw new IllegalStateException();
        }
    }

    final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
        int mEnd;
        int mIndex;
        boolean mIsEntryValid = false;

        MapIterator() {
            this.mEnd = MapCollections.this.colGetSize() - 1;
            this.mIndex = -1;
        }

        public boolean hasNext() {
            return this.mIndex < this.mEnd;
        }

        @Override // java.util.Iterator
        public Map.Entry<K, V> next() {
            if (hasNext()) {
                this.mIndex++;
                this.mIsEntryValid = true;
                return this;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.mIsEntryValid) {
                MapCollections.this.colRemoveAt(this.mIndex);
                this.mIndex--;
                this.mEnd--;
                this.mIsEntryValid = false;
                return;
            }
            throw new IllegalStateException();
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            if (this.mIsEntryValid) {
                return (K) MapCollections.this.colGetEntry(this.mIndex, 0);
            }
            throw new IllegalStateException(MapCollections.MESSAGE);
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            if (this.mIsEntryValid) {
                return (V) MapCollections.this.colGetEntry(this.mIndex, 1);
            }
            throw new IllegalStateException(MapCollections.MESSAGE);
        }

        @Override // java.util.Map.Entry
        public V setValue(V object) {
            if (this.mIsEntryValid) {
                return (V) MapCollections.this.colSetValue(this.mIndex, object);
            }
            throw new IllegalStateException(MapCollections.MESSAGE);
        }

        public boolean equals(Object obj) {
            if (!this.mIsEntryValid) {
                throw new IllegalStateException(MapCollections.MESSAGE);
            } else if (!(obj instanceof Map.Entry)) {
                return false;
            } else {
                Map.Entry<?, ?> entry = (Map.Entry) obj;
                if (!Objects.equals(entry.getKey(), MapCollections.this.colGetEntry(this.mIndex, 0)) || !Objects.equals(entry.getValue(), MapCollections.this.colGetEntry(this.mIndex, 1))) {
                    return false;
                }
                return true;
            }
        }

        public int hashCode() {
            if (this.mIsEntryValid) {
                int i = 0;
                Object key = MapCollections.this.colGetEntry(this.mIndex, 0);
                Object value = MapCollections.this.colGetEntry(this.mIndex, 1);
                int hashCode = key == null ? 0 : key.hashCode();
                if (value != null) {
                    i = value.hashCode();
                }
                return i ^ hashCode;
            }
            throw new IllegalStateException(MapCollections.MESSAGE);
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    final class EntrySet implements Set<Map.Entry<K, V>> {
        EntrySet() {
        }

        @Override // java.util.Collection, java.util.Set
        public /* bridge */ /* synthetic */ boolean add(Object obj) {
            return add((Map.Entry) ((Map.Entry) obj));
        }

        public boolean add(Map.Entry<K, V> entry) {
            throw new UnsupportedOperationException();
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: huawei.android.widget.appbar.MapCollections */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.util.Collection, java.util.Set
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

        public boolean contains(Object object) {
            if (!(object instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) object;
            int index = MapCollections.this.colIndexOfKey(entry.getKey());
            if (index < 0) {
                return false;
            }
            return Objects.equals(MapCollections.this.colGetEntry(index, 1), entry.getValue());
        }

        @Override // java.util.Collection, java.util.Set
        public boolean containsAll(Collection<?> collection) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                if (!contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return MapCollections.this.colGetSize() == 0;
        }

        @Override // java.util.Collection, java.util.Set, java.lang.Iterable
        public Iterator<Map.Entry<K, V>> iterator() {
            return new MapIterator();
        }

        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection, java.util.Set
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection, java.util.Set
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return MapCollections.this.colGetSize();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection, java.util.Set
        public <T> T[] toArray(T[] tArr) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object object) {
            return MapCollections.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int result = 0;
            for (int i = MapCollections.this.colGetSize() - 1; i >= 0; i--) {
                int i2 = 0;
                Object key = MapCollections.this.colGetEntry(i, 0);
                Object value = MapCollections.this.colGetEntry(i, 1);
                int hashCode = key == null ? 0 : key.hashCode();
                if (value != null) {
                    i2 = value.hashCode();
                }
                result += i2 ^ hashCode;
            }
            return result;
        }
    }

    final class KeySet implements Set<K> {
        KeySet() {
        }

        @Override // java.util.Collection, java.util.Set
        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection, java.util.Set
        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            MapCollections.this.colClear();
        }

        public boolean contains(Object object) {
            return MapCollections.this.colIndexOfKey(object) >= 0;
        }

        @Override // java.util.Collection, java.util.Set
        public boolean containsAll(Collection<?> collection) {
            return MapCollections.containsAllHelper(MapCollections.this.colGetMap(), collection);
        }

        public boolean isEmpty() {
            return MapCollections.this.colGetSize() == 0;
        }

        @Override // java.util.Collection, java.util.Set, java.lang.Iterable
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

        @Override // java.util.Collection, java.util.Set
        public boolean removeAll(Collection<?> collection) {
            return MapCollections.removeAllHelper(MapCollections.this.colGetMap(), collection);
        }

        @Override // java.util.Collection, java.util.Set
        public boolean retainAll(Collection<?> collection) {
            return MapCollections.retainAllHelper(MapCollections.this.colGetMap(), collection);
        }

        public int size() {
            return MapCollections.this.colGetSize();
        }

        public Object[] toArray() {
            return MapCollections.this.toArrayHelper(0);
        }

        @Override // java.util.Collection, java.util.Set
        public <T> T[] toArray(T[] array) {
            return (T[]) MapCollections.this.toArrayHelper(array, 0);
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

    final class ValuesCollection implements Collection<V> {
        ValuesCollection() {
        }

        @Override // java.util.Collection
        public boolean add(V v) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.Collection
        public boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            MapCollections.this.colClear();
        }

        public boolean contains(Object object) {
            return MapCollections.this.colIndexOfValue(object) >= 0;
        }

        @Override // java.util.Collection
        public boolean containsAll(Collection<?> collection) {
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                if (!contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return MapCollections.this.colGetSize() == 0;
        }

        @Override // java.util.Collection, java.lang.Iterable
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

        @Override // java.util.Collection
        public boolean removeAll(Collection<?> collection) {
            int size = MapCollections.this.colGetSize();
            boolean isChanged = false;
            int i = 0;
            while (i < size) {
                int index = i;
                if (collection.contains(MapCollections.this.colGetEntry(index, 1))) {
                    MapCollections.this.colRemoveAt(index);
                    index--;
                    size--;
                    isChanged = true;
                }
                i = index + 1;
            }
            return isChanged;
        }

        @Override // java.util.Collection
        public boolean retainAll(Collection<?> collection) {
            int size = MapCollections.this.colGetSize();
            boolean isChanged = false;
            int i = 0;
            while (i < size) {
                int index = i;
                if (!collection.contains(MapCollections.this.colGetEntry(index, 1))) {
                    MapCollections.this.colRemoveAt(index);
                    index--;
                    size--;
                    isChanged = true;
                }
                i = index + 1;
            }
            return isChanged;
        }

        public int size() {
            return MapCollections.this.colGetSize();
        }

        public Object[] toArray() {
            return MapCollections.this.toArrayHelper(1);
        }

        @Override // java.util.Collection
        public <T> T[] toArray(T[] array) {
            return (T[]) MapCollections.this.toArrayHelper(array, 1);
        }
    }

    public static <K, V> boolean containsAllHelper(Map<K, V> map, Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!map.containsKey(it.next())) {
                return false;
            }
        }
        return true;
    }

    public static <K, V> boolean removeAllHelper(Map<K, V> map, Collection<?> collection) {
        int oldSize = map.size();
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            map.remove(iterator.next());
        }
        return oldSize != map.size();
    }

    public static <K, V> boolean retainAllHelper(Map<K, V> map, Collection<?> collection) {
        int oldSize = map.size();
        Iterator<K> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            if (!collection.contains(iterator.next())) {
                iterator.remove();
            }
        }
        return oldSize != map.size();
    }

    public Object[] toArrayHelper(int offset) {
        int size = colGetSize();
        Object[] results = new Object[size];
        for (int i = 0; i < size; i++) {
            results[i] = colGetEntry(i, offset);
        }
        return results;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v9, types: [java.lang.Object[]] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public <T> T[] toArrayHelper(T[] array, int offset) {
        T[] temArrays = array;
        int size = colGetSize();
        if (temArrays.length < size) {
            temArrays = (Object[]) Array.newInstance(temArrays.getClass().getComponentType(), size);
        }
        for (int i = 0; i < size; i++) {
            temArrays[i] = colGetEntry(i, offset);
        }
        if (temArrays.length > size) {
            temArrays[size] = null;
        }
        return temArrays;
    }

    public static <T> boolean equalsSetHelper(Set<T> set, Object object) {
        if (set == object) {
            return true;
        }
        if (set == null || object == null || !(object instanceof Set)) {
            return false;
        }
        Set<?> set2 = (Set) object;
        try {
            if (set.size() != set2.size() || !set.containsAll(set2)) {
                return false;
            }
            return true;
        } catch (ClassCastException e) {
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
