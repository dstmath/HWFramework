package android.support.v4.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ArraySet<E> implements Collection<E>, Set<E> {
    private static final int BASE_SIZE = 4;
    private static final int CACHE_SIZE = 10;
    private static final boolean DEBUG = false;
    private static final int[] INT = new int[0];
    private static final Object[] OBJECT = new Object[0];
    private static final String TAG = "ArraySet";
    private static Object[] sBaseCache;
    private static int sBaseCacheSize;
    private static Object[] sTwiceBaseCache;
    private static int sTwiceBaseCacheSize;
    /* access modifiers changed from: private */
    public Object[] mArray;
    private MapCollections<E, E> mCollections;
    private int[] mHashes;
    /* access modifiers changed from: private */
    public int mSize;

    private int indexOf(Object key, int hash) {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = ContainerHelpers.binarySearch(this.mHashes, N, hash);
        if (index < 0 || key.equals(this.mArray[index])) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == hash) {
            if (key.equals(this.mArray[end])) {
                return end;
            }
            end++;
        }
        int i = index - 1;
        while (i >= 0 && this.mHashes[i] == hash) {
            if (key.equals(this.mArray[i])) {
                return i;
            }
            i--;
        }
        return ~end;
    }

    private int indexOfNull() {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = ContainerHelpers.binarySearch(this.mHashes, N, 0);
        if (index < 0 || this.mArray[index] == null) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == 0) {
            if (this.mArray[end] == null) {
                return end;
            }
            end++;
        }
        int i = index - 1;
        while (i >= 0 && this.mHashes[i] == 0) {
            if (this.mArray[i] == null) {
                return i;
            }
            i--;
        }
        return ~end;
    }

    private void allocArrays(int size) {
        if (size == 8) {
            synchronized (ArraySet.class) {
                if (sTwiceBaseCache != null) {
                    Object[] array = sTwiceBaseCache;
                    this.mArray = array;
                    sTwiceBaseCache = (Object[]) array[0];
                    this.mHashes = (int[]) array[1];
                    array[1] = null;
                    array[0] = null;
                    sTwiceBaseCacheSize--;
                    return;
                }
            }
        } else if (size == 4) {
            synchronized (ArraySet.class) {
                if (sBaseCache != null) {
                    Object[] array2 = sBaseCache;
                    this.mArray = array2;
                    sBaseCache = (Object[]) array2[0];
                    this.mHashes = (int[]) array2[1];
                    array2[1] = null;
                    array2[0] = null;
                    sBaseCacheSize--;
                    return;
                }
            }
        }
        this.mHashes = new int[size];
        this.mArray = new Object[size];
    }

    private static void freeArrays(int[] hashes, Object[] array, int size) {
        if (hashes.length == 8) {
            synchronized (ArraySet.class) {
                if (sTwiceBaseCacheSize < 10) {
                    array[0] = sTwiceBaseCache;
                    array[1] = hashes;
                    for (int i = size - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    sTwiceBaseCache = array;
                    sTwiceBaseCacheSize++;
                }
            }
        } else if (hashes.length == 4) {
            synchronized (ArraySet.class) {
                if (sBaseCacheSize < 10) {
                    array[0] = sBaseCache;
                    array[1] = hashes;
                    for (int i2 = size - 1; i2 >= 2; i2--) {
                        array[i2] = null;
                    }
                    sBaseCache = array;
                    sBaseCacheSize++;
                }
            }
        }
    }

    public ArraySet() {
        this(0);
    }

    public ArraySet(int capacity) {
        if (capacity == 0) {
            this.mHashes = INT;
            this.mArray = OBJECT;
        } else {
            allocArrays(capacity);
        }
        this.mSize = 0;
    }

    public ArraySet(@Nullable ArraySet<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    public ArraySet(@Nullable Collection<E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    public void clear() {
        if (this.mSize != 0) {
            freeArrays(this.mHashes, this.mArray, this.mSize);
            this.mHashes = INT;
            this.mArray = OBJECT;
            this.mSize = 0;
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        if (this.mHashes.length < minimumCapacity) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(minimumCapacity);
            if (this.mSize > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, this.mSize);
                System.arraycopy(oarray, 0, this.mArray, 0, this.mSize);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
    }

    public boolean contains(Object key) {
        if (indexOf(key) >= 0) {
            return true;
        }
        return DEBUG;
    }

    public int indexOf(Object key) {
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
    }

    @Nullable
    public E valueAt(int index) {
        return this.mArray[index];
    }

    public boolean isEmpty() {
        if (this.mSize <= 0) {
            return true;
        }
        return DEBUG;
    }

    public boolean add(@Nullable E value) {
        int index;
        int hash;
        if (value == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = value.hashCode();
            index = indexOf(value, hash);
        }
        if (index >= 0) {
            return DEBUG;
        }
        int index2 = ~index;
        if (this.mSize >= this.mHashes.length) {
            int i = 4;
            if (this.mSize >= 8) {
                i = (this.mSize >> 1) + this.mSize;
            } else if (this.mSize >= 4) {
                i = 8;
            }
            int n = i;
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            if (this.mHashes.length > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, this.mArray, 0, oarray.length);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
        if (index2 < this.mSize) {
            System.arraycopy(this.mHashes, index2, this.mHashes, index2 + 1, this.mSize - index2);
            System.arraycopy(this.mArray, index2, this.mArray, index2 + 1, this.mSize - index2);
        }
        this.mHashes[index2] = hash;
        this.mArray[index2] = value;
        this.mSize++;
        return true;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void append(E value) {
        int index = this.mSize;
        int hash = value == null ? 0 : value.hashCode();
        if (index >= this.mHashes.length) {
            throw new IllegalStateException("Array is full");
        } else if (index <= 0 || this.mHashes[index - 1] <= hash) {
            this.mSize = index + 1;
            this.mHashes[index] = hash;
            this.mArray[index] = value;
        } else {
            add(value);
        }
    }

    public void addAll(@NonNull ArraySet<? extends E> array) {
        int N = array.mSize;
        ensureCapacity(this.mSize + N);
        int i = 0;
        if (this.mSize != 0) {
            while (true) {
                int i2 = i;
                if (i2 < N) {
                    add(array.valueAt(i2));
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        } else if (N > 0) {
            System.arraycopy(array.mHashes, 0, this.mHashes, 0, N);
            System.arraycopy(array.mArray, 0, this.mArray, 0, N);
            this.mSize = N;
        }
    }

    public boolean remove(Object object) {
        int index = indexOf(object);
        if (index < 0) {
            return DEBUG;
        }
        removeAt(index);
        return true;
    }

    public E removeAt(int index) {
        Object old = this.mArray[index];
        if (this.mSize <= 1) {
            freeArrays(this.mHashes, this.mArray, this.mSize);
            this.mHashes = INT;
            this.mArray = OBJECT;
            this.mSize = 0;
        } else {
            int i = 8;
            if (this.mHashes.length <= 8 || this.mSize >= this.mHashes.length / 3) {
                this.mSize--;
                if (index < this.mSize) {
                    System.arraycopy(this.mHashes, index + 1, this.mHashes, index, this.mSize - index);
                    System.arraycopy(this.mArray, index + 1, this.mArray, index, this.mSize - index);
                }
                this.mArray[this.mSize] = null;
            } else {
                if (this.mSize > 8) {
                    i = (this.mSize >> 1) + this.mSize;
                }
                int n = i;
                int[] ohashes = this.mHashes;
                Object[] oarray = this.mArray;
                allocArrays(n);
                this.mSize--;
                if (index > 0) {
                    System.arraycopy(ohashes, 0, this.mHashes, 0, index);
                    System.arraycopy(oarray, 0, this.mArray, 0, index);
                }
                if (index < this.mSize) {
                    System.arraycopy(ohashes, index + 1, this.mHashes, index, this.mSize - index);
                    System.arraycopy(oarray, index + 1, this.mArray, index, this.mSize - index);
                }
            }
        }
        return old;
    }

    public boolean removeAll(ArraySet<? extends E> array) {
        int N = array.mSize;
        int originalSize = this.mSize;
        for (int i = 0; i < N; i++) {
            remove(array.valueAt(i));
        }
        if (originalSize != this.mSize) {
            return true;
        }
        return DEBUG;
    }

    public int size() {
        return this.mSize;
    }

    @NonNull
    public Object[] toArray() {
        Object[] result = new Object[this.mSize];
        System.arraycopy(this.mArray, 0, result, 0, this.mSize);
        return result;
    }

    @NonNull
    public <T> T[] toArray(@NonNull T[] array) {
        if (array.length < this.mSize) {
            array = (Object[]) Array.newInstance(array.getClass().getComponentType(), this.mSize);
        }
        System.arraycopy(this.mArray, 0, array, 0, this.mSize);
        if (array.length > this.mSize) {
            array[this.mSize] = null;
        }
        return array;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Set)) {
            return DEBUG;
        }
        Set<?> set = (Set) object;
        if (size() != set.size()) {
            return DEBUG;
        }
        int i = 0;
        while (i < this.mSize) {
            try {
                if (!set.contains(valueAt(i))) {
                    return DEBUG;
                }
                i++;
            } catch (NullPointerException e) {
                return DEBUG;
            } catch (ClassCastException e2) {
                return DEBUG;
            }
        }
        return true;
    }

    public int hashCode() {
        int[] hashes = this.mHashes;
        int result = 0;
        int s = this.mSize;
        for (int i = 0; i < s; i++) {
            result += hashes[i];
        }
        return result;
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 14);
        buffer.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Set)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    private MapCollections<E, E> getCollection() {
        if (this.mCollections == null) {
            this.mCollections = new MapCollections<E, E>() {
                /* access modifiers changed from: protected */
                public int colGetSize() {
                    return ArraySet.this.mSize;
                }

                /* access modifiers changed from: protected */
                public Object colGetEntry(int index, int offset) {
                    return ArraySet.this.mArray[index];
                }

                /* access modifiers changed from: protected */
                public int colIndexOfKey(Object key) {
                    return ArraySet.this.indexOf(key);
                }

                /* access modifiers changed from: protected */
                public int colIndexOfValue(Object value) {
                    return ArraySet.this.indexOf(value);
                }

                /* access modifiers changed from: protected */
                public Map<E, E> colGetMap() {
                    throw new UnsupportedOperationException("not a map");
                }

                /* access modifiers changed from: protected */
                public void colPut(E key, E e) {
                    ArraySet.this.add(key);
                }

                /* access modifiers changed from: protected */
                public E colSetValue(int index, E e) {
                    throw new UnsupportedOperationException("not a map");
                }

                /* access modifiers changed from: protected */
                public void colRemoveAt(int index) {
                    ArraySet.this.removeAt(index);
                }

                /* access modifiers changed from: protected */
                public void colClear() {
                    ArraySet.this.clear();
                }
            };
        }
        return this.mCollections;
    }

    public Iterator<E> iterator() {
        return getCollection().getKeySet().iterator();
    }

    public boolean containsAll(@NonNull Collection<?> collection) {
        for (Object contains : collection) {
            if (!contains(contains)) {
                return DEBUG;
            }
        }
        return true;
    }

    public boolean addAll(@NonNull Collection<? extends E> collection) {
        ensureCapacity(this.mSize + collection.size());
        boolean added = DEBUG;
        for (E value : collection) {
            added |= add(value);
        }
        return added;
    }

    public boolean removeAll(@NonNull Collection<?> collection) {
        boolean removed = DEBUG;
        for (Object value : collection) {
            removed |= remove(value);
        }
        return removed;
    }

    public boolean retainAll(@NonNull Collection<?> collection) {
        boolean removed = DEBUG;
        for (int i = this.mSize - 1; i >= 0; i--) {
            if (!collection.contains(this.mArray[i])) {
                removeAt(i);
                removed = true;
            }
        }
        return removed;
    }
}
