package android.util;

import android.annotation.UnsupportedAppUsage;
import java.lang.annotation.RCWeakRef;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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
    @UnsupportedAppUsage(maxTargetSdk = 28)
    Object[] mArray;
    @RCWeakRef
    MapCollections<E, E> mCollections;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int[] mHashes;
    final boolean mIdentityHashCode;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mSize;

    @UnsupportedAppUsage(maxTargetSdk = 28)
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

    @UnsupportedAppUsage(maxTargetSdk = 28)
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

    @UnsupportedAppUsage(maxTargetSdk = 28)
    private void allocArrays(int size) {
        if (size == 8) {
            synchronized (ArraySet.class) {
                if (sTwiceBaseCache != null) {
                    Object[] array = sTwiceBaseCache;
                    try {
                        this.mArray = array;
                        sTwiceBaseCache = (Object[]) array[0];
                        this.mHashes = (int[]) array[1];
                        array[1] = null;
                        array[0] = null;
                        sTwiceBaseCacheSize--;
                        return;
                    } catch (ClassCastException e) {
                        Slog.wtf(TAG, "Found corrupt ArraySet cache: [0]=" + array[0] + " [1]=" + array[1]);
                        sTwiceBaseCache = null;
                        sTwiceBaseCacheSize = 0;
                    }
                }
            }
        } else if (size == 4) {
            synchronized (ArraySet.class) {
                if (sBaseCache != null) {
                    Object[] array2 = sBaseCache;
                    try {
                        this.mArray = array2;
                        sBaseCache = (Object[]) array2[0];
                        this.mHashes = (int[]) array2[1];
                        array2[1] = null;
                        array2[0] = null;
                        sBaseCacheSize--;
                        return;
                    } catch (ClassCastException e2) {
                        Slog.wtf(TAG, "Found corrupt ArraySet cache: [0]=" + array2[0] + " [1]=" + array2[1]);
                        sBaseCache = null;
                        sBaseCacheSize = 0;
                    }
                }
            }
        }
        this.mHashes = new int[size];
        this.mArray = new Object[size];
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
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

    public ArraySet(Collection<? extends E> set) {
        this();
        if (set != null) {
            addAll(set);
        }
    }

    @Override // java.util.Collection, java.util.Set
    public void clear() {
        int i = this.mSize;
        if (i != 0) {
            freeArrays(this.mHashes, this.mArray, i);
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
            int i = this.mSize;
            if (i > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, i);
                System.arraycopy(oarray, 0, this.mArray, 0, this.mSize);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
    }

    @Override // java.util.Collection, java.util.Set
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
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            return valueAtUnchecked(index);
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public E valueAtUnchecked(int index) {
        return (E) this.mArray[index];
    }

    @Override // java.util.Collection, java.util.Set
    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    @Override // java.util.Collection, java.util.Set
    public boolean add(E value) {
        int index;
        int hash;
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
        int index2 = ~index;
        int i = this.mSize;
        if (i >= this.mHashes.length) {
            int n = 4;
            if (i >= 8) {
                n = (i >> 1) + i;
            } else if (i >= 4) {
                n = 8;
            }
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            int[] iArr = this.mHashes;
            if (iArr.length > 0) {
                System.arraycopy(ohashes, 0, iArr, 0, ohashes.length);
                System.arraycopy(oarray, 0, this.mArray, 0, oarray.length);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
        int i2 = this.mSize;
        if (index2 < i2) {
            int[] iArr2 = this.mHashes;
            System.arraycopy(iArr2, index2, iArr2, index2 + 1, i2 - index2);
            Object[] objArr = this.mArray;
            System.arraycopy(objArr, index2, objArr, index2 + 1, this.mSize - index2);
        }
        this.mHashes[index2] = hash;
        this.mArray[index2] = value;
        this.mSize++;
        return true;
    }

    public void append(E value) {
        int hash;
        int index = this.mSize;
        if (value == null) {
            hash = 0;
        } else {
            hash = this.mIdentityHashCode ? System.identityHashCode(value) : value.hashCode();
        }
        int[] iArr = this.mHashes;
        if (index >= iArr.length) {
            throw new IllegalStateException("Array is full");
        } else if (index <= 0 || iArr[index - 1] <= hash) {
            this.mSize = index + 1;
            this.mHashes[index] = hash;
            this.mArray[index] = value;
        } else {
            add(value);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: android.util.ArraySet<E> */
    /* JADX WARN: Multi-variable type inference failed */
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

    @Override // java.util.Collection, java.util.Set
    public boolean remove(Object object) {
        int index = indexOf(object);
        if (index < 0) {
            return false;
        }
        removeAt(index);
        return true;
    }

    private boolean shouldShrink() {
        int[] iArr = this.mHashes;
        return iArr.length > 8 && this.mSize < iArr.length / 3;
    }

    private int getNewShrunkenSize() {
        int i = this.mSize;
        if (i > 8) {
            return (i >> 1) + i;
        }
        return 8;
    }

    public E removeAt(int index) {
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            E e = (E) this.mArray[index];
            if (this.mSize <= 1) {
                clear();
            } else if (shouldShrink()) {
                int n = getNewShrunkenSize();
                int[] ohashes = this.mHashes;
                Object[] oarray = this.mArray;
                allocArrays(n);
                this.mSize--;
                if (index > 0) {
                    System.arraycopy(ohashes, 0, this.mHashes, 0, index);
                    System.arraycopy(oarray, 0, this.mArray, 0, index);
                }
                int i = this.mSize;
                if (index < i) {
                    System.arraycopy(ohashes, index + 1, this.mHashes, index, i - index);
                    System.arraycopy(oarray, index + 1, this.mArray, index, this.mSize - index);
                }
            } else {
                this.mSize--;
                int i2 = this.mSize;
                if (index < i2) {
                    int[] iArr = this.mHashes;
                    System.arraycopy(iArr, index + 1, iArr, index, i2 - index);
                    Object[] objArr = this.mArray;
                    System.arraycopy(objArr, index + 1, objArr, index, this.mSize - index);
                }
                this.mArray[this.mSize] = null;
            }
            return e;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public boolean removeAll(ArraySet<? extends E> array) {
        int N = array.mSize;
        int originalSize = this.mSize;
        for (int i = 0; i < N; i++) {
            remove(array.valueAt(i));
        }
        return originalSize != this.mSize;
    }

    @Override // java.util.Collection
    public boolean removeIf(Predicate<? super E> filter) {
        int i;
        if (this.mSize == 0) {
            return false;
        }
        int replaceIndex = 0;
        int numRemoved = 0;
        int i2 = 0;
        while (true) {
            i = this.mSize;
            if (i2 >= i) {
                break;
            }
            if (filter.test(this.mArray[i2])) {
                numRemoved++;
            } else {
                if (replaceIndex != i2) {
                    Object[] objArr = this.mArray;
                    objArr[replaceIndex] = objArr[i2];
                    int[] iArr = this.mHashes;
                    iArr[replaceIndex] = iArr[i2];
                }
                replaceIndex++;
            }
            i2++;
        }
        if (numRemoved == 0) {
            return false;
        }
        if (numRemoved == i) {
            clear();
            return true;
        }
        this.mSize = i - numRemoved;
        if (!shouldShrink()) {
            int i3 = this.mSize;
            while (true) {
                Object[] objArr2 = this.mArray;
                if (i3 >= objArr2.length) {
                    break;
                }
                objArr2[i3] = null;
                i3++;
            }
        } else {
            int n = getNewShrunkenSize();
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            System.arraycopy(ohashes, 0, this.mHashes, 0, this.mSize);
            System.arraycopy(oarray, 0, this.mArray, 0, this.mSize);
        }
        return true;
    }

    @Override // java.util.Collection, java.util.Set
    public int size() {
        return this.mSize;
    }

    @Override // java.util.Collection, java.util.Set
    public Object[] toArray() {
        int i = this.mSize;
        Object[] result = new Object[i];
        System.arraycopy(this.mArray, 0, result, 0, i);
        return result;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v7, types: [java.lang.Object[]] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // java.util.Collection, java.util.Set
    public <T> T[] toArray(T[] array) {
        if (array.length < this.mSize) {
            array = (Object[]) Array.newInstance(array.getClass().getComponentType(), this.mSize);
        }
        System.arraycopy(this.mArray, 0, array, 0, this.mSize);
        int length = array.length;
        int i = this.mSize;
        if (length > i) {
            array[i] = null;
        }
        return array;
    }

    @Override // java.util.Collection, java.lang.Object, java.util.Set
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
        for (int i = 0; i < this.mSize; i++) {
            try {
                if (!set.contains(valueAt(i))) {
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

    @Override // java.util.Collection, java.lang.Object, java.util.Set
    public int hashCode() {
        int[] hashes = this.mHashes;
        int result = 0;
        int s = this.mSize;
        for (int i = 0; i < s; i++) {
            result += hashes[i];
        }
        return result;
    }

    @Override // java.lang.Object
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
        MapCollections<E, E> res = this.mCollections;
        if (res != null) {
            return res;
        }
        MapCollections<E, E> res2 = new MapCollections<E, E>() {
            /* class android.util.ArraySet.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public int colGetSize() {
                return ArraySet.this.mSize;
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public Object colGetEntry(int index, int offset) {
                return ArraySet.this.mArray[index];
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public int colIndexOfKey(Object key) {
                return ArraySet.this.indexOf(key);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public int colIndexOfValue(Object value) {
                return ArraySet.this.indexOf(value);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public Map<E, E> colGetMap() {
                throw new UnsupportedOperationException("not a map");
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public void colPut(E key, E e) {
                ArraySet.this.add(key);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public E colSetValue(int index, E e) {
                throw new UnsupportedOperationException("not a map");
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public void colRemoveAt(int index) {
                ArraySet.this.removeAt(index);
            }

            /* access modifiers changed from: protected */
            @Override // android.util.MapCollections
            public void colClear() {
                ArraySet.this.clear();
            }
        };
        this.mCollections = res2;
        return res2;
    }

    @Override // java.util.Collection, java.lang.Iterable, java.util.Set
    public Iterator<E> iterator() {
        return getCollection().getKeySet().iterator();
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

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: android.util.ArraySet<E> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Collection, java.util.Set
    public boolean addAll(Collection<? extends E> collection) {
        ensureCapacity(this.mSize + collection.size());
        boolean added = false;
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            added |= add(it.next());
        }
        return added;
    }

    @Override // java.util.Collection, java.util.Set
    public boolean removeAll(Collection<?> collection) {
        boolean removed = false;
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            removed |= remove(it.next());
        }
        return removed;
    }

    @Override // java.util.Collection, java.util.Set
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
