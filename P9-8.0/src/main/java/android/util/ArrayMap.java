package android.util;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import libcore.util.EmptyArray;

public final class ArrayMap<K, V> implements Map<K, V> {
    private static final int BASE_SIZE = 4;
    private static final int CACHE_SIZE = 10;
    private static final boolean CONCURRENT_MODIFICATION_EXCEPTIONS = true;
    private static final boolean DEBUG = false;
    public static final ArrayMap EMPTY = new ArrayMap(-1);
    static final int[] EMPTY_IMMUTABLE_INTS = new int[0];
    private static final String TAG = "ArrayMap";
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;
    Object[] mArray;
    MapCollections<K, V> mCollections;
    int[] mHashes;
    final boolean mIdentityHashCode;
    int mSize;

    private static int binarySearchHashes(int[] hashes, int N, int hash) {
        try {
            return ContainerHelpers.binarySearch(hashes, N, hash);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
        }
    }

    int indexOf(Object key, int hash) {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = binarySearchHashes(this.mHashes, N, hash);
        if (index < 0 || key.equals(this.mArray[index << 1])) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == hash) {
            if (key.equals(this.mArray[end << 1])) {
                return end;
            }
            end++;
        }
        int i = index - 1;
        while (i >= 0 && this.mHashes[i] == hash) {
            if (key.equals(this.mArray[i << 1])) {
                return i;
            }
            i--;
        }
        return ~end;
    }

    int indexOfNull() {
        int N = this.mSize;
        if (N == 0) {
            return -1;
        }
        int index = binarySearchHashes(this.mHashes, N, 0);
        if (index < 0 || this.mArray[index << 1] == null) {
            return index;
        }
        int end = index + 1;
        while (end < N && this.mHashes[end] == 0) {
            if (this.mArray[end << 1] == null) {
                return end;
            }
            end++;
        }
        int i = index - 1;
        while (i >= 0 && this.mHashes[i] == 0) {
            if (this.mArray[i << 1] == null) {
                return i;
            }
            i--;
        }
        return ~end;
    }

    private void allocArrays(int size) {
        if (this.mHashes == EMPTY_IMMUTABLE_INTS) {
            throw new UnsupportedOperationException("ArrayMap is immutable");
        }
        Class cls;
        Object[] array;
        if (size == 8) {
            cls = ArrayMap.class;
            synchronized (cls) {
                if (mTwiceBaseCache != null) {
                    try {
                        array = mTwiceBaseCache;
                        this.mArray = array;
                        mTwiceBaseCache = (Object[]) array[0];
                        this.mHashes = (int[]) array[1];
                        array[1] = null;
                        array[0] = null;
                        mTwiceBaseCacheSize--;
                    } catch (ClassCastException e) {
                        Log.e(TAG, "allocArrays occured exception: " + e);
                        mTwiceBaseCache = null;
                        mTwiceBaseCacheSize = 0;
                    }
                }
            }
            return;
        }
        if (size == 4) {
            cls = ArrayMap.class;
            synchronized (cls) {
                if (mBaseCache != null) {
                    try {
                        array = mBaseCache;
                        this.mArray = array;
                        mBaseCache = (Object[]) array[0];
                        this.mHashes = (int[]) array[1];
                        array[1] = null;
                        array[0] = null;
                        mBaseCacheSize--;
                    } catch (ClassCastException e2) {
                        Log.e(TAG, "allocArrays occured exception1: " + e2);
                        mBaseCache = null;
                        mBaseCacheSize = 0;
                    }
                }
            }
            return;
        }
        this.mHashes = new int[size];
        this.mArray = new Object[(size << 1)];
        return;
        this.mHashes = new int[size];
        this.mArray = new Object[(size << 1)];
        return;
    }

    private static void freeArrays(int[] hashes, Object[] array, int size) {
        Class cls;
        int i;
        if (hashes.length == 8) {
            cls = ArrayMap.class;
            synchronized (cls) {
                if (mTwiceBaseCacheSize < 10) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                }
            }
        } else if (hashes.length == 4) {
            cls = ArrayMap.class;
            synchronized (cls) {
                if (mBaseCacheSize < 10) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                }
            }
        } else {
            return;
        }
    }

    public ArrayMap() {
        this(0, false);
    }

    public ArrayMap(int capacity) {
        this(capacity, false);
    }

    public ArrayMap(int capacity, boolean identityHashCode) {
        this.mIdentityHashCode = identityHashCode;
        if (capacity < 0) {
            this.mHashes = EMPTY_IMMUTABLE_INTS;
            this.mArray = EmptyArray.OBJECT;
        } else if (capacity == 0) {
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
        } else {
            allocArrays(capacity);
        }
        this.mSize = 0;
    }

    public ArrayMap(ArrayMap<K, V> map) {
        this();
        if (map != null) {
            putAll((ArrayMap) map);
        }
    }

    public void clear() {
        if (this.mSize > 0) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            int osize = this.mSize;
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
            this.mSize = 0;
            freeArrays(ohashes, oarray, osize);
        }
        if (this.mSize > 0) {
            throw new ConcurrentModificationException();
        }
    }

    public void erase() {
        if (this.mSize > 0) {
            int N = this.mSize << 1;
            Object[] array = this.mArray;
            for (int i = 0; i < N; i++) {
                array[i] = null;
            }
            this.mSize = 0;
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        int osize = this.mSize;
        if (this.mHashes.length < minimumCapacity) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(minimumCapacity);
            if (this.mSize > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, osize);
                System.arraycopy(oarray, 0, this.mArray, 0, osize << 1);
            }
            freeArrays(ohashes, oarray, osize);
        }
        if (this.mSize != osize) {
            throw new ConcurrentModificationException();
        }
    }

    public boolean containsKey(Object key) {
        return indexOfKey(key) >= 0;
    }

    public int indexOfKey(Object key) {
        if (key == null) {
            return indexOfNull();
        }
        return indexOf(key, this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode());
    }

    int indexOfValue(Object value) {
        int N = this.mSize * 2;
        Object[] array = this.mArray;
        int i;
        if (value == null) {
            for (i = 1; i < N; i += 2) {
                if (array[i] == null) {
                    return i >> 1;
                }
            }
        } else {
            for (i = 1; i < N; i += 2) {
                if (value.equals(array[i])) {
                    return i >> 1;
                }
            }
        }
        return -1;
    }

    public boolean containsValue(Object value) {
        return indexOfValue(value) >= 0;
    }

    public V get(Object key) {
        int index = indexOfKey(key);
        return index >= 0 ? this.mArray[(index << 1) + 1] : null;
    }

    public K keyAt(int index) {
        return this.mArray[index << 1];
    }

    public V valueAt(int index) {
        return this.mArray[(index << 1) + 1];
    }

    public V setValueAt(int index, V value) {
        index = (index << 1) + 1;
        V old = this.mArray[index];
        this.mArray[index] = value;
        return old;
    }

    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    public V put(K key, V value) {
        int hash;
        int index;
        int osize = this.mSize;
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
            index = indexOf(key, hash);
        }
        if (index >= 0) {
            index = (index << 1) + 1;
            V old = this.mArray[index];
            this.mArray[index] = value;
            return old;
        }
        int oldlength = this.mHashes.length;
        index = ~index;
        if (osize >= this.mHashes.length) {
            int n = osize >= 8 ? osize + (osize >> 1) : osize >= 4 ? 8 : 4;
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            if (osize != this.mSize) {
                throw new ConcurrentModificationException();
            }
            if (this.mHashes.length > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, this.mArray, 0, oarray.length);
            }
            freeArrays(ohashes, oarray, osize);
        }
        if (index < osize) {
            System.arraycopy(this.mHashes, index, this.mHashes, index + 1, osize - index);
            System.arraycopy(this.mArray, index << 1, this.mArray, (index + 1) << 1, (this.mSize - index) << 1);
        }
        if (osize != this.mSize || index >= this.mHashes.length) {
            Log.e(TAG, "oldlength: " + oldlength + ", newlength: " + this.mHashes.length + ", osize: " + osize + ", mSize: " + this.mSize + ", index: " + index);
            throw new ConcurrentModificationException();
        }
        this.mHashes[index] = hash;
        this.mArray[index << 1] = key;
        this.mArray[(index << 1) + 1] = value;
        this.mSize++;
        return null;
    }

    public void append(K key, V value) {
        int index = this.mSize;
        int hash = key == null ? 0 : this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
        if (index >= this.mHashes.length) {
            throw new IllegalStateException("Array is full");
        } else if (index <= 0 || this.mHashes[index - 1] <= hash) {
            this.mSize = index + 1;
            this.mHashes[index] = hash;
            index <<= 1;
            this.mArray[index] = key;
            this.mArray[index + 1] = value;
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "New hash " + hash + " is before end of array hash " + this.mHashes[index - 1] + " at index " + index + " key " + key, e);
            put(key, value);
        }
    }

    public void validate() {
        int N = this.mSize;
        if (N > 1) {
            int basehash = this.mHashes[0];
            int basei = 0;
            for (int i = 1; i < N; i++) {
                int hash = this.mHashes[i];
                if (hash != basehash) {
                    basehash = hash;
                    basei = i;
                } else {
                    Object cur = this.mArray[i << 1];
                    int j = i - 1;
                    while (j >= basei) {
                        Object prev = this.mArray[j << 1];
                        if (cur == prev) {
                            throw new IllegalArgumentException("Duplicate key in ArrayMap: " + cur);
                        } else if (cur == null || prev == null || !cur.equals(prev)) {
                            j--;
                        } else {
                            throw new IllegalArgumentException("Duplicate key in ArrayMap: " + cur);
                        }
                    }
                    continue;
                }
            }
        }
    }

    public void putAll(ArrayMap<? extends K, ? extends V> array) {
        int N = array.mSize;
        try {
            ensureCapacity(this.mSize + N);
            if (this.mSize != 0) {
                for (int i = 0; i < N; i++) {
                    put(array.keyAt(i), array.valueAt(i));
                }
            } else if (N > 0) {
                System.arraycopy(array.mHashes, 0, this.mHashes, 0, N);
                System.arraycopy(array.mArray, 0, this.mArray, 0, N << 1);
                this.mSize = N;
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "ensureCapacity allocArrays occured exception: " + e);
        }
    }

    public V remove(Object key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }
        return null;
    }

    public V removeAt(int index) {
        int nsize;
        Object old = this.mArray[(index << 1) + 1];
        int osize = this.mSize;
        if (osize <= 1) {
            freeArrays(this.mHashes, this.mArray, osize);
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
            nsize = 0;
        } else {
            nsize = osize - 1;
            if (this.mHashes.length <= 8 || this.mSize >= this.mHashes.length / 3) {
                if (index < nsize) {
                    System.arraycopy(this.mHashes, index + 1, this.mHashes, index, nsize - index);
                    System.arraycopy(this.mArray, (index + 1) << 1, this.mArray, index << 1, (nsize - index) << 1);
                }
                this.mArray[nsize << 1] = null;
                this.mArray[(nsize << 1) + 1] = null;
            } else {
                int n = osize > 8 ? osize + (osize >> 1) : 8;
                int[] ohashes = this.mHashes;
                Object[] oarray = this.mArray;
                allocArrays(n);
                if (osize != this.mSize) {
                    throw new ConcurrentModificationException();
                }
                if (index > 0) {
                    System.arraycopy(ohashes, 0, this.mHashes, 0, index);
                    System.arraycopy(oarray, 0, this.mArray, 0, index << 1);
                }
                if (index < nsize) {
                    System.arraycopy(ohashes, index + 1, this.mHashes, index, nsize - index);
                    System.arraycopy(oarray, (index + 1) << 1, this.mArray, index << 1, (nsize - index) << 1);
                }
            }
        }
        if (osize != this.mSize) {
            throw new ConcurrentModificationException();
        }
        this.mSize = nsize;
        return old;
    }

    public int size() {
        return this.mSize;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Map)) {
            return false;
        }
        Map<?, ?> map = (Map) object;
        if (size() != map.size()) {
            return false;
        }
        int i = 0;
        while (i < this.mSize) {
            try {
                K key = keyAt(i);
                V mine = valueAt(i);
                Object theirs = map.get(key);
                if (mine == null) {
                    if (theirs != null || (map.containsKey(key) ^ 1) != 0) {
                        return false;
                    }
                } else if (!mine.equals(theirs)) {
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
        Object[] array = this.mArray;
        int result = 0;
        int i = 0;
        int v = 1;
        int s = this.mSize;
        while (i < s) {
            Object value = array[v];
            result += (value == null ? 0 : value.hashCode()) ^ hashes[i];
            i++;
            v += 2;
        }
        return result;
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 28);
        buffer.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            ArrayMap key = keyAt(i);
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)");
            }
            buffer.append('=');
            ArrayMap value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    private MapCollections<K, V> getCollection() {
        if (this.mCollections == null) {
            this.mCollections = new MapCollections<K, V>() {
                protected int colGetSize() {
                    return ArrayMap.this.mSize;
                }

                protected Object colGetEntry(int index, int offset) {
                    return ArrayMap.this.mArray[(index << 1) + offset];
                }

                protected int colIndexOfKey(Object key) {
                    return ArrayMap.this.indexOfKey(key);
                }

                protected int colIndexOfValue(Object value) {
                    return ArrayMap.this.indexOfValue(value);
                }

                protected Map<K, V> colGetMap() {
                    return ArrayMap.this;
                }

                protected void colPut(K key, V value) {
                    ArrayMap.this.put(key, value);
                }

                protected V colSetValue(int index, V value) {
                    return ArrayMap.this.setValueAt(index, value);
                }

                protected void colRemoveAt(int index) {
                    ArrayMap.this.removeAt(index);
                }

                protected void colClear() {
                    ArrayMap.this.clear();
                }
            };
        }
        return this.mCollections;
    }

    public boolean containsAll(Collection<?> collection) {
        return MapCollections.containsAllHelper(this, collection);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean removeAll(Collection<?> collection) {
        return MapCollections.removeAllHelper(this, collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return MapCollections.retainAllHelper(this, collection);
    }

    public Set<Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    public Collection<V> values() {
        return getCollection().getValues();
    }
}
