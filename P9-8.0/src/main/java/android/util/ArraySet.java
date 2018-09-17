package android.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import libcore.util.EmptyArray;

public final class ArraySet<E> implements Collection<E>, Set<E> {
    private static final int BASE_SIZE = 4;
    private static final int CACHE_SIZE = 10;
    private static final boolean DEBUG = false;
    private static final String TAG = "ArraySet";
    static Object[] sBaseCache;
    static int sBaseCacheSize;
    static Object[] sTwiceBaseCache;
    static int sTwiceBaseCacheSize;
    Object[] mArray;
    MapCollections<E, E> mCollections;
    int[] mHashes;
    final boolean mIdentityHashCode;
    int mSize;

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
        Class cls;
        Object[] array;
        if (size == 8) {
            cls = ArraySet.class;
            synchronized (cls) {
                if (sTwiceBaseCache != null) {
                    array = sTwiceBaseCache;
                    try {
                        this.mArray = array;
                        sTwiceBaseCache = (Object[]) array[0];
                        this.mHashes = (int[]) array[1];
                        array[1] = null;
                        array[0] = null;
                        sTwiceBaseCacheSize--;
                    } catch (ClassCastException e) {
                        Slog.wtf(TAG, "Found corrupt ArraySet cache: [0]=" + array[0] + " [1]=" + array[1]);
                        sTwiceBaseCache = null;
                        sTwiceBaseCacheSize = 0;
                    }
                }
            }
            return;
        }
        if (size == 4) {
            cls = ArraySet.class;
            synchronized (cls) {
                if (sBaseCache != null) {
                    array = sBaseCache;
                    try {
                        this.mArray = array;
                        sBaseCache = (Object[]) array[0];
                        this.mHashes = (int[]) array[1];
                        array[1] = null;
                        array[0] = null;
                        sBaseCacheSize--;
                    } catch (ClassCastException e2) {
                        Log.e(TAG, "catch a ClassCastException in function allocArrays");
                        Slog.wtf(TAG, "Found corrupt ArraySet cache: [0]=" + array[0] + " [1]=" + array[1]);
                        sBaseCache = null;
                        sBaseCacheSize = 0;
                    }
                }
            }
            return;
        }
        this.mHashes = new int[size];
        this.mArray = new Object[size];
        return;
        this.mHashes = new int[size];
        this.mArray = new Object[size];
        return;
    }

    private static void freeArrays(int[] hashes, Object[] array, int size) {
        Class cls;
        int i;
        if (hashes.length == 8) {
            cls = ArraySet.class;
            synchronized (cls) {
                if (sTwiceBaseCacheSize < 10) {
                    array[0] = sTwiceBaseCache;
                    array[1] = hashes;
                    for (i = size - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    sTwiceBaseCache = array;
                    sTwiceBaseCacheSize++;
                }
            }
        } else if (hashes.length == 4) {
            cls = ArraySet.class;
            synchronized (cls) {
                if (sBaseCacheSize < 10) {
                    array[0] = sBaseCache;
                    array[1] = hashes;
                    for (i = size - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    sBaseCache = array;
                    sBaseCacheSize++;
                }
            }
        } else {
            return;
        }
    }

    public ArraySet() {
        this(0, false);
    }

    public ArraySet(int capacity) {
        this(capacity, false);
    }

    public ArraySet(int capacity, boolean identityHashCode) {
        this.mIdentityHashCode = identityHashCode;
        if (capacity == 0) {
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
        } else {
            allocArrays(capacity);
        }
        this.mSize = 0;
    }

    public ArraySet(ArraySet<E> set) {
        this();
        if (set != null) {
            addAll((ArraySet) set);
        }
    }

    public ArraySet(Collection<E> set) {
        this();
        if (set != null) {
            addAll((Collection) set);
        }
    }

    public void clear() {
        if (this.mSize != 0) {
            freeArrays(this.mHashes, this.mArray, this.mSize);
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
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
        return indexOf(key) >= 0;
    }

    public int indexOf(Object key) {
        if (key == null) {
            return indexOfNull();
        }
        return indexOf(key, this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode());
    }

    public E valueAt(int index) {
        return this.mArray[index];
    }

    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    public boolean add(E value) {
        int hash;
        int index;
        if (value == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = this.mIdentityHashCode ? System.identityHashCode(value) : value.hashCode();
            index = indexOf(value, hash);
        }
        if (index >= 0) {
            return false;
        }
        index = ~index;
        if (this.mSize >= this.mHashes.length) {
            int n = this.mSize >= 8 ? this.mSize + (this.mSize >> 1) : this.mSize >= 4 ? 8 : 4;
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            if (this.mHashes.length > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, this.mArray, 0, oarray.length);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
        if (index < this.mSize) {
            System.arraycopy(this.mHashes, index, this.mHashes, index + 1, this.mSize - index);
            System.arraycopy(this.mArray, index, this.mArray, index + 1, this.mSize - index);
        }
        this.mHashes[index] = hash;
        this.mArray[index] = value;
        this.mSize++;
        return true;
    }

    public void append(E value) {
        int index = this.mSize;
        int hash = value == null ? 0 : this.mIdentityHashCode ? System.identityHashCode(value) : value.hashCode();
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

    public void addAll(ArraySet<? extends E> array) {
        int N = array.mSize;
        ensureCapacity(this.mSize + N);
        if (this.mSize != 0) {
            for (int i = 0; i < N; i++) {
                add(array.valueAt(i));
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
            return false;
        }
        removeAt(index);
        return true;
    }

    public E removeAt(int index) {
        Object old = this.mArray[index];
        if (this.mSize <= 1) {
            freeArrays(this.mHashes, this.mArray, this.mSize);
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
            this.mSize = 0;
        } else if (this.mHashes.length <= 8 || this.mSize >= this.mHashes.length / 3) {
            this.mSize--;
            if (index < this.mSize) {
                System.arraycopy(this.mHashes, index + 1, this.mHashes, index, this.mSize - index);
                System.arraycopy(this.mArray, index + 1, this.mArray, index, this.mSize - index);
            }
            this.mArray[this.mSize] = null;
        } else {
            int n = this.mSize > 8 ? this.mSize + (this.mSize >> 1) : 8;
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
        return old;
    }

    public boolean removeAll(ArraySet<? extends E> array) {
        int N = array.mSize;
        int originalSize = this.mSize;
        for (int i = 0; i < N; i++) {
            remove(array.valueAt(i));
        }
        return originalSize != this.mSize;
    }

    public int size() {
        return this.mSize;
    }

    public Object[] toArray() {
        Object[] result = new Object[this.mSize];
        System.arraycopy(this.mArray, 0, result, 0, this.mSize);
        return result;
    }

    public <T> T[] toArray(T[] array) {
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
            return false;
        }
        Set<?> set = (Set) object;
        if (size() != set.size()) {
            return false;
        }
        int i = 0;
        while (i < this.mSize) {
            try {
                if (!set.contains(valueAt(i))) {
                    return false;
                }
                i++;
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e2) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int[] hashes = this.mHashes;
        int result = 0;
        for (int i = 0; i < this.mSize; i++) {
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
            ArraySet value = valueAt(i);
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
                protected int colGetSize() {
                    return ArraySet.this.mSize;
                }

                protected Object colGetEntry(int index, int offset) {
                    return ArraySet.this.mArray[index];
                }

                protected int colIndexOfKey(Object key) {
                    return ArraySet.this.indexOf(key);
                }

                protected int colIndexOfValue(Object value) {
                    return ArraySet.this.indexOf(value);
                }

                protected Map<E, E> colGetMap() {
                    throw new UnsupportedOperationException("not a map");
                }

                protected void colPut(E key, E e) {
                    ArraySet.this.add(key);
                }

                protected E colSetValue(int index, E e) {
                    throw new UnsupportedOperationException("not a map");
                }

                protected void colRemoveAt(int index) {
                    ArraySet.this.removeAt(index);
                }

                protected void colClear() {
                    ArraySet.this.clear();
                }
            };
        }
        return this.mCollections;
    }

    public Iterator<E> iterator() {
        return getCollection().getKeySet().iterator();
    }

    public boolean containsAll(Collection<?> collection) {
        for (Object contains : collection) {
            if (!contains(contains)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> collection) {
        ensureCapacity(this.mSize + collection.size());
        boolean added = false;
        for (E value : collection) {
            added |= add(value);
        }
        return added;
    }

    public boolean removeAll(Collection<?> collection) {
        boolean removed = false;
        for (Object value : collection) {
            removed |= remove(value);
        }
        return removed;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean removed = false;
        for (int i = this.mSize - 1; i >= 0; i--) {
            if (!collection.contains(this.mArray[i])) {
                removeAt(i);
                removed = true;
            }
        }
        return removed;
    }
}
