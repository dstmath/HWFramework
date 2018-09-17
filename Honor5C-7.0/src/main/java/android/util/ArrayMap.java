package android.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import libcore.util.EmptyArray;

public final class ArrayMap<K, V> implements Map<K, V> {
    private static final int BASE_SIZE = 4;
    private static final int CACHE_SIZE = 10;
    private static final boolean DEBUG = false;
    public static final ArrayMap EMPTY = null;
    static final int[] EMPTY_IMMUTABLE_INTS = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.ArrayMap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.ArrayMap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.ArrayMap.<clinit>():void");
    }

    int indexOf(java.lang.Object r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.ArrayMap.indexOf(java.lang.Object, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.ArrayMap.indexOf(java.lang.Object, int):int");
    }

    int indexOfNull() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.ArrayMap.indexOfNull():int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.ArrayMap.indexOfNull():int");
    }

    public V put(K r1, V r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.ArrayMap.put(java.lang.Object, java.lang.Object):V
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.ArrayMap.put(java.lang.Object, java.lang.Object):V");
    }

    private void allocArrays(int size) {
        if (this.mHashes == EMPTY_IMMUTABLE_INTS) {
            throw new UnsupportedOperationException("ArrayMap is immutable");
        }
        if (size == 8) {
            Class cls = ArrayMap.class;
            synchronized (cls) {
                if (mTwiceBaseCache != null) {
                    Object[] array = mTwiceBaseCache;
                    this.mArray = array;
                    mTwiceBaseCache = (Object[]) array[0];
                    this.mHashes = (int[]) array[1];
                    array[1] = null;
                    array[0] = null;
                    mTwiceBaseCacheSize--;
                }
            }
            return;
        }
        if (size == BASE_SIZE) {
            cls = ArrayMap.class;
            synchronized (cls) {
                if (mBaseCache != null) {
                    array = mBaseCache;
                    this.mArray = array;
                    mBaseCache = (Object[]) array[0];
                    this.mHashes = (int[]) array[1];
                    array[1] = null;
                    array[0] = null;
                    mBaseCacheSize--;
                }
            }
            return;
        }
        this.mHashes = new int[size];
        this.mArray = new Object[(size << 1)];
        this.mHashes = new int[size];
        this.mArray = new Object[(size << 1)];
    }

    private static void freeArrays(int[] hashes, Object[] array, int size) {
        Class cls;
        int i;
        if (hashes.length == 8) {
            cls = ArrayMap.class;
            synchronized (cls) {
            }
            if (mTwiceBaseCacheSize < CACHE_SIZE) {
                array[0] = mTwiceBaseCache;
                array[1] = hashes;
                for (i = (size << 1) - 1; i >= 2; i--) {
                    array[i] = null;
                }
                mTwiceBaseCache = array;
                mTwiceBaseCacheSize++;
            }
        } else if (hashes.length == BASE_SIZE) {
            cls = ArrayMap.class;
            synchronized (cls) {
            }
            if (mBaseCacheSize < CACHE_SIZE) {
                array[0] = mBaseCache;
                array[1] = hashes;
                for (i = (size << 1) - 1; i >= 2; i--) {
                    array[i] = null;
                }
                mBaseCache = array;
                mBaseCacheSize++;
            }
        } else {
            return;
        }
    }

    public ArrayMap() {
        this(0, DEBUG);
    }

    public ArrayMap(int capacity) {
        this(capacity, DEBUG);
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
            freeArrays(this.mHashes, this.mArray, this.mSize);
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
            this.mSize = 0;
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
        if (this.mHashes.length < minimumCapacity) {
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(minimumCapacity);
            if (this.mSize > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, this.mSize);
                System.arraycopy(oarray, 0, this.mArray, 0, this.mSize << 1);
            }
            freeArrays(ohashes, oarray, this.mSize);
        }
    }

    public boolean containsKey(Object key) {
        return indexOfKey(key) >= 0 ? true : DEBUG;
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
        return indexOfValue(value) >= 0 ? true : DEBUG;
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
        return this.mSize <= 0 ? true : DEBUG;
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

    public V remove(Object key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }
        return null;
    }

    public V removeAt(int index) {
        Object old = this.mArray[(index << 1) + 1];
        if (this.mSize <= 1) {
            freeArrays(this.mHashes, this.mArray, this.mSize);
            this.mHashes = EmptyArray.INT;
            this.mArray = EmptyArray.OBJECT;
            this.mSize = 0;
        } else if (this.mHashes.length <= 8 || this.mSize >= this.mHashes.length / 3) {
            this.mSize--;
            if (index < this.mSize) {
                System.arraycopy(this.mHashes, index + 1, this.mHashes, index, this.mSize - index);
                System.arraycopy(this.mArray, (index + 1) << 1, this.mArray, index << 1, (this.mSize - index) << 1);
            }
            this.mArray[this.mSize << 1] = null;
            this.mArray[(this.mSize << 1) + 1] = null;
        } else {
            int n = this.mSize > 8 ? this.mSize + (this.mSize >> 1) : 8;
            int[] ohashes = this.mHashes;
            Object[] oarray = this.mArray;
            allocArrays(n);
            this.mSize--;
            if (index > 0) {
                System.arraycopy(ohashes, 0, this.mHashes, 0, index);
                System.arraycopy(oarray, 0, this.mArray, 0, index << 1);
            }
            if (index < this.mSize) {
                System.arraycopy(ohashes, index + 1, this.mHashes, index, this.mSize - index);
                System.arraycopy(oarray, (index + 1) << 1, this.mArray, index << 1, (this.mSize - index) << 1);
            }
        }
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
            return DEBUG;
        }
        Map<?, ?> map = (Map) object;
        if (size() != map.size()) {
            return DEBUG;
        }
        int i = 0;
        while (i < this.mSize) {
            try {
                K key = keyAt(i);
                V mine = valueAt(i);
                Object theirs = map.get(key);
                if (mine == null) {
                    if (theirs != null || !map.containsKey(key)) {
                        return DEBUG;
                    }
                } else if (!mine.equals(theirs)) {
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
