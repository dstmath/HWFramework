package android.util;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.util.ArrayUtils;
import java.lang.annotation.RCWeakRef;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;
import libcore.util.EmptyArray;

public final class ArrayMap<K, V> implements Map<K, V> {
    private static final int BASE_SIZE = 4;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static final int CACHE_SIZE = 10;
    private static final boolean CONCURRENT_MODIFICATION_EXCEPTIONS = true;
    private static final boolean DEBUG = false;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public static final ArrayMap EMPTY = new ArrayMap(-1);
    @UnsupportedAppUsage(maxTargetSdk = 28)
    static final int[] EMPTY_IMMUTABLE_INTS = new int[0];
    private static final String TAG = "ArrayMap";
    @UnsupportedAppUsage(maxTargetSdk = 28)
    static Object[] mBaseCache;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    static int mBaseCacheSize;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    static Object[] mTwiceBaseCache;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    static int mTwiceBaseCacheSize;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    Object[] mArray;
    @RCWeakRef
    MapCollections<K, V> mCollections;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int[] mHashes;
    final boolean mIdentityHashCode;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mSize;

    private static int binarySearchHashes(int[] hashes, int N, int hash) {
        try {
            return ContainerHelpers.binarySearch(hashes, N, hash);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public int indexOf(Object key, int hash) {
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

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public int indexOfNull() {
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

    @UnsupportedAppUsage(maxTargetSdk = 28)
    private void allocArrays(int size) {
        if (this.mHashes != EMPTY_IMMUTABLE_INTS) {
            if (size == 8) {
                synchronized (ArrayMap.class) {
                    if (mTwiceBaseCache != null) {
                        try {
                            Object[] array = mTwiceBaseCache;
                            this.mArray = array;
                            mTwiceBaseCache = (Object[]) array[0];
                            this.mHashes = (int[]) array[1];
                            array[1] = null;
                            array[0] = null;
                            mTwiceBaseCacheSize--;
                            return;
                        } catch (ClassCastException e) {
                            Log.e(TAG, "allocArrays occured exception: " + e);
                            mTwiceBaseCache = null;
                            mTwiceBaseCacheSize = 0;
                        }
                    }
                }
            } else if (size == 4) {
                synchronized (ArrayMap.class) {
                    if (mBaseCache != null) {
                        try {
                            Object[] array2 = mBaseCache;
                            this.mArray = array2;
                            mBaseCache = (Object[]) array2[0];
                            this.mHashes = (int[]) array2[1];
                            array2[1] = null;
                            array2[0] = null;
                            mBaseCacheSize--;
                            return;
                        } catch (ClassCastException e2) {
                            Log.e(TAG, "allocArrays occured exception1: " + e2);
                            mBaseCache = null;
                            mBaseCacheSize = 0;
                        }
                    }
                }
            }
            this.mHashes = new int[size];
            this.mArray = new Object[(size << 1)];
            return;
        }
        throw new UnsupportedOperationException("ArrayMap is immutable");
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static void freeArrays(int[] hashes, Object[] array, int size) {
        if (hashes.length == 8) {
            synchronized (ArrayMap.class) {
                if (mTwiceBaseCacheSize < 10) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                }
            }
        } else if (hashes.length == 4) {
            synchronized (ArrayMap.class) {
                if (mBaseCacheSize < 10) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i2 = (size << 1) - 1; i2 >= 2; i2--) {
                        array[i2] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                }
            }
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

    @Override // java.util.Map
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
        int i = this.mSize;
        if (i > 0) {
            int N = i << 1;
            Object[] array = this.mArray;
            for (int i2 = 0; i2 < N; i2++) {
                array[i2] = null;
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

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        return indexOfKey(key) >= 0;
    }

    public int indexOfKey(Object key) {
        if (key == null) {
            return indexOfNull();
        }
        return indexOf(key, this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode());
    }

    public int indexOfValue(Object value) {
        int N = this.mSize * 2;
        Object[] array = this.mArray;
        if (value == null) {
            for (int i = 1; i < N; i += 2) {
                if (array[i] == null) {
                    return i >> 1;
                }
            }
            return -1;
        }
        for (int i2 = 1; i2 < N; i2 += 2) {
            if (value.equals(array[i2])) {
                return i2 >> 1;
            }
        }
        return -1;
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        return indexOfValue(value) >= 0;
    }

    @Override // java.util.Map
    public V get(Object key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return (V) this.mArray[(index << 1) + 1];
        }
        return null;
    }

    public K keyAt(int index) {
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            return (K) this.mArray[index << 1];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public V valueAt(int index) {
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            return (V) this.mArray[(index << 1) + 1];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public V setValueAt(int index, V value) {
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            int index2 = (index << 1) + 1;
            Object[] objArr = this.mArray;
            V old = (V) objArr[index2];
            objArr[index2] = value;
            return old;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    @Override // java.util.Map
    public V put(K key, V value) {
        int index;
        int hash;
        int osize = this.mSize;
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
            index = indexOf(key, hash);
        }
        if (index >= 0) {
            int index2 = (index << 1) + 1;
            Object[] objArr = this.mArray;
            V old = (V) objArr[index2];
            objArr[index2] = value;
            return old;
        }
        int index3 = ~index;
        if (osize >= this.mHashes.length) {
            int n = 4;
            if (osize >= 8) {
                n = (osize >> 1) + osize;
            } else if (osize >= 4) {
                n = 8;
            }
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            if (osize == this.mSize) {
                int[] iArr = this.mHashes;
                if (iArr.length > 0) {
                    System.arraycopy(ohashes, 0, iArr, 0, ohashes.length);
                    System.arraycopy(oarray, 0, this.mArray, 0, oarray.length);
                }
                freeArrays(ohashes, oarray, osize);
            } else {
                throw new ConcurrentModificationException();
            }
        }
        if (index3 < osize) {
            int[] iArr2 = this.mHashes;
            System.arraycopy(iArr2, index3, iArr2, index3 + 1, osize - index3);
            Object[] objArr2 = this.mArray;
            System.arraycopy(objArr2, index3 << 1, objArr2, (index3 + 1) << 1, (this.mSize - index3) << 1);
        }
        int i = this.mSize;
        if (osize == i) {
            int[] iArr3 = this.mHashes;
            if (index3 < iArr3.length) {
                iArr3[index3] = hash;
                Object[] objArr3 = this.mArray;
                objArr3[index3 << 1] = key;
                objArr3[(index3 << 1) + 1] = value;
                this.mSize = i + 1;
                return null;
            }
        }
        throw new ConcurrentModificationException();
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    public void append(K key, V value) {
        int hash;
        int index = this.mSize;
        if (key == null) {
            hash = 0;
        } else {
            hash = this.mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
        }
        int[] iArr = this.mHashes;
        if (index >= iArr.length) {
            throw new IllegalStateException("Array is full");
        } else if (index <= 0 || iArr[index - 1] <= hash) {
            this.mSize = index + 1;
            this.mHashes[index] = hash;
            int index2 = index << 1;
            Object[] objArr = this.mArray;
            objArr[index2] = key;
            objArr[index2 + 1] = value;
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "New hash " + hash + " is before end of array hash " + this.mHashes[index - 1] + " at index " + index + " key " + ((Object) key), e);
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
                    for (int j = i - 1; j >= basei; j--) {
                        Object prev = this.mArray[j << 1];
                        if (cur == prev) {
                            throw new IllegalArgumentException("Duplicate key in ArrayMap: " + cur);
                        } else if (cur != null && prev != null && cur.equals(prev)) {
                            throw new IllegalArgumentException("Duplicate key in ArrayMap: " + cur);
                        }
                    }
                    continue;
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: android.util.ArrayMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public void putAll(ArrayMap<? extends K, ? extends V> array) {
        int N = array.mSize;
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
    }

    @Override // java.util.Map
    public V remove(Object key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }
        return null;
    }

    public V removeAt(int index) {
        int nsize;
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            V v = (V) this.mArray[(index << 1) + 1];
            int osize = this.mSize;
            if (osize <= 1) {
                int[] ohashes = this.mHashes;
                Object[] oarray = this.mArray;
                this.mHashes = EmptyArray.INT;
                this.mArray = EmptyArray.OBJECT;
                freeArrays(ohashes, oarray, osize);
                nsize = 0;
            } else {
                int nsize2 = osize - 1;
                int[] iArr = this.mHashes;
                int n = 8;
                if (iArr.length <= 8 || this.mSize >= iArr.length / 3) {
                    if (index < nsize2) {
                        int[] iArr2 = this.mHashes;
                        System.arraycopy(iArr2, index + 1, iArr2, index, nsize2 - index);
                        Object[] objArr = this.mArray;
                        System.arraycopy(objArr, (index + 1) << 1, objArr, index << 1, (nsize2 - index) << 1);
                    }
                    Object[] objArr2 = this.mArray;
                    objArr2[nsize2 << 1] = null;
                    objArr2[(nsize2 << 1) + 1] = null;
                } else {
                    if (osize > 8) {
                        n = osize + (osize >> 1);
                    }
                    int[] ohashes2 = this.mHashes;
                    Object[] oarray2 = this.mArray;
                    allocArrays(n);
                    if (osize == this.mSize) {
                        if (index > 0) {
                            System.arraycopy(ohashes2, 0, this.mHashes, 0, index);
                            System.arraycopy(oarray2, 0, this.mArray, 0, index << 1);
                        }
                        if (index < nsize2) {
                            System.arraycopy(ohashes2, index + 1, this.mHashes, index, nsize2 - index);
                            System.arraycopy(oarray2, (index + 1) << 1, this.mArray, index << 1, (nsize2 - index) << 1);
                        }
                    } else {
                        throw new ConcurrentModificationException();
                    }
                }
                nsize = nsize2;
            }
            if (osize == this.mSize) {
                this.mSize = nsize;
                return v;
            }
            throw new ConcurrentModificationException();
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override // java.util.Map
    public int size() {
        return this.mSize;
    }

    @Override // java.util.Map, java.lang.Object
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
        for (int i = 0; i < this.mSize; i++) {
            try {
                K key = keyAt(i);
                V mine = valueAt(i);
                Object theirs = map.get(key);
                if (mine == null) {
                    if (theirs != null || !map.containsKey(key)) {
                        return false;
                    }
                } else if (!mine.equals(theirs)) {
                    return false;
                }
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e2) {
                return false;
            }
        }
        return true;
    }

    @Override // java.util.Map, java.lang.Object
    public int hashCode() {
        int[] hashes = this.mHashes;
        Object[] array = this.mArray;
        int result = 0;
        int i = 0;
        int v = 1;
        int s = this.mSize;
        while (i < s) {
            Object value = array[v];
            result += hashes[i] ^ (value == null ? 0 : value.hashCode());
            i++;
            v += 2;
        }
        return result;
    }

    @Override // java.lang.Object
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
            Object key = keyAt(i);
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)");
            }
            buffer.append('=');
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(ArrayUtils.deepToString(value));
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    private MapCollections<K, V> getCollection() {
        MapCollections<K, V> res = this.mCollections;
        if (res != null) {
            return res;
        }
        MapCollections<K, V> res2 = new MapCollections<K, V>() {
            /* class android.util.ArrayMap.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public int colGetSize() {
                return ArrayMap.this.mSize;
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public Object colGetEntry(int index, int offset) {
                return ArrayMap.this.mArray[(index << 1) + offset];
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public int colIndexOfKey(Object key) {
                return ArrayMap.this.indexOfKey(key);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public int colIndexOfValue(Object value) {
                return ArrayMap.this.indexOfValue(value);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public Map<K, V> colGetMap() {
                return ArrayMap.this;
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public void colPut(K key, V value) {
                ArrayMap.this.put(key, value);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public V colSetValue(int index, V value) {
                return (V) ArrayMap.this.setValueAt(index, value);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public void colRemoveAt(int index) {
                ArrayMap.this.removeAt(index);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public void colClear() {
                ArrayMap.this.clear();
            }
        };
        this.mCollections = res2;
        return res2;
    }

    public boolean containsAll(Collection<?> collection) {
        return MapCollections.containsAllHelper(this, collection);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: android.util.ArrayMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean removeAll(Collection<?> collection) {
        return MapCollections.removeAllHelper(this, collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return MapCollections.retainAllHelper(this, collection);
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    @Override // java.util.Map
    public Collection<V> values() {
        return getCollection().getValues();
    }
}
