package ohos.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public final class LightweightMap<K, V> implements Map<K, V> {
    private static ArrayMemManager memManager = new ArrayMemManager();
    private int[] hashes;
    private Object[] keyArray;
    private int size;
    private LightweightMap<K, V>.SubMembers subMembers;
    private boolean useIdentityHash;
    private Object[] valueArray;

    public LightweightMap() {
        this(0, false);
    }

    public LightweightMap(int i) {
        this(i, false);
    }

    public LightweightMap(int i, boolean z) {
        this.hashes = EmptyArray.INT;
        this.keyArray = EmptyArray.OBJECT;
        this.valueArray = EmptyArray.OBJECT;
        this.useIdentityHash = z;
        alloc(i);
    }

    public LightweightMap(LightweightMap<? extends K, ? extends V> lightweightMap) {
        this(lightweightMap.size, lightweightMap.useIdentityHash);
        if (!lightweightMap.isEmpty()) {
            putAll((LightweightMap) lightweightMap);
        }
    }

    @Override // java.util.Map
    public void clear() {
        if (this.size != 0) {
            freeToCache(this.hashes, this.keyArray, this.valueArray);
            this.hashes = EmptyArray.INT;
            this.keyArray = EmptyArray.OBJECT;
            this.valueArray = EmptyArray.OBJECT;
            this.size = 0;
        }
    }

    public boolean containsAll(Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!containsKey(it.next())) {
                return false;
            }
        }
        return true;
    }

    @Override // java.util.Map
    public boolean containsKey(Object obj) {
        return indexOfKey(obj) >= 0;
    }

    @Override // java.util.Map
    public boolean containsValue(Object obj) {
        return indexOfValue(obj) >= 0;
    }

    public void ensureCapacity(int i) {
        if (this.hashes.length < i) {
            sizeCopy(this.size, i);
        }
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        LightweightMap<K, V>.SubMembers subMembers2 = this.subMembers;
        if (subMembers2 == null) {
            subMembers2 = new SubMembers();
            this.subMembers = subMembers2;
        }
        this.subMembers = subMembers2;
        return this.subMembers.getEntrySet();
    }

    @Override // java.util.Map
    public V get(Object obj) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey >= 0) {
            return (V) this.valueArray[indexOfKey];
        }
        return null;
    }

    public int indexOfKey(Object obj) {
        return indexOf(obj, keyHashCode(obj));
    }

    public int indexOfValue(Object obj) {
        int i = 0;
        while (i < this.size) {
            if (Objects.equals(this.valueArray[i], obj)) {
                return i;
            }
            i++;
        }
        return ~i;
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.size <= 0;
    }

    public K keyAt(int i) {
        Object[] objArr = this.keyArray;
        if (i < 0) {
            i += this.size;
        }
        return (K) objArr[i];
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        LightweightMap<K, V>.SubMembers subMembers2 = this.subMembers;
        if (subMembers2 == null) {
            subMembers2 = new SubMembers();
            this.subMembers = subMembers2;
        }
        this.subMembers = subMembers2;
        return this.subMembers.getKeySet();
    }

    @Override // java.util.Map
    public V put(K k, V v) {
        int keyHashCode = keyHashCode(k);
        int indexOf = indexOf(k, keyHashCode);
        if (indexOf >= 0) {
            Object[] objArr = this.valueArray;
            V v2 = (V) objArr[indexOf];
            objArr[indexOf] = v;
            return v2;
        }
        int i = ~indexOf;
        expandIfNecessary();
        int i2 = this.size;
        if (i < i2) {
            int[] iArr = this.hashes;
            int i3 = i + 1;
            System.arraycopy(iArr, i, iArr, i3, i2 - i);
            Object[] objArr2 = this.keyArray;
            System.arraycopy(objArr2, i, objArr2, i3, this.size - i);
            Object[] objArr3 = this.valueArray;
            System.arraycopy(objArr3, i, objArr3, i3, this.size - i);
        }
        this.hashes[i] = keyHashCode;
        this.keyArray[i] = k;
        this.valueArray[i] = v;
        this.size++;
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: ohos.utils.LightweightMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public void putAll(LightweightMap<? extends K, ? extends V> lightweightMap) {
        int size2 = lightweightMap.size();
        for (int i = 0; i < size2; i++) {
            put(lightweightMap.keyAt(i), lightweightMap.valueAt(i));
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: ohos.utils.LightweightMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.size + map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override // java.util.Map
    public V remove(Object obj) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey < 0) {
            return null;
        }
        return removeAt(indexOfKey);
    }

    public boolean removeAll(Collection<?> collection) {
        boolean z = false;
        for (int i = this.size - 1; i >= 0; i--) {
            if (collection.contains(this.keyArray[i])) {
                removeAt(i);
                z = true;
            }
        }
        return z;
    }

    public V removeAt(int i) {
        if (i < 0) {
            i += this.size;
        }
        int i2 = this.size;
        if (i >= i2 || i < 0) {
            return null;
        }
        V v = (V) this.valueArray[i];
        if (i2 <= 1) {
            clear();
        } else {
            shrinkIfNecessary();
            this.size--;
            int i3 = this.size;
            if (i < i3) {
                int[] iArr = this.hashes;
                int i4 = i + 1;
                System.arraycopy(iArr, i4, iArr, i, i3 - i);
                Object[] objArr = this.keyArray;
                System.arraycopy(objArr, i4, objArr, i, this.size - i);
                Object[] objArr2 = this.valueArray;
                System.arraycopy(objArr2, i4, objArr2, i, this.size - i);
            }
            Object[] objArr3 = this.keyArray;
            int i5 = this.size;
            objArr3[i5] = null;
            this.valueArray[i5] = null;
        }
        return v;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean z = false;
        for (int i = this.size - 1; i >= 0; i--) {
            if (!collection.contains(this.keyArray[i])) {
                removeAt(i);
                z = true;
            }
        }
        return z;
    }

    public V setValueAt(int i, V v) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        Object[] objArr = this.valueArray;
        V v2 = (V) objArr[i];
        objArr[i] = v;
        return v2;
    }

    @Override // java.util.Map
    public int size() {
        return this.size;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0027: APUT  (r3v2 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v1 java.lang.String) */
    @Override // java.lang.Object
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.size; i++) {
            Object[] objArr = new Object[3];
            Object[] objArr2 = this.keyArray;
            String str = "null";
            objArr[0] = objArr2[i] == null ? str : objArr2[i].toString();
            Object[] objArr3 = this.valueArray;
            if (objArr3[i] != null) {
                str = objArr3[i].toString();
            }
            objArr[1] = str;
            objArr[2] = Locale.ROOT;
            arrayList.add(String.format("%s: %s", objArr));
        }
        return "{" + String.join(", ", arrayList) + "}";
    }

    public V valueAt(int i) {
        Object[] objArr = this.valueArray;
        if (i < 0) {
            i += this.size;
        }
        return (V) objArr[i];
    }

    @Override // java.util.Map
    public Collection<V> values() {
        LightweightMap<K, V>.SubMembers subMembers2 = this.subMembers;
        if (subMembers2 == null) {
            subMembers2 = new SubMembers();
            this.subMembers = subMembers2;
        }
        this.subMembers = subMembers2;
        return this.subMembers.getValueSet();
    }

    private void alloc(int i) {
        if (i == 0) {
            this.hashes = EmptyArray.INT;
            this.keyArray = EmptyArray.OBJECT;
            this.valueArray = EmptyArray.OBJECT;
            this.size = 0;
            return;
        }
        Object[] allocFromCache = memManager.allocFromCache(i);
        if (allocFromCache != null) {
            this.keyArray = allocFromCache;
            Object[] objArr = this.keyArray;
            this.hashes = (int[]) objArr[1];
            this.valueArray = (Object[]) objArr[2];
            return;
        }
        this.hashes = new int[i];
        this.keyArray = new Object[i];
        this.valueArray = new Object[i];
    }

    private void expandIfNecessary() {
        int expandCapacity = memManager.getExpandCapacity(this.hashes.length, this.size);
        if (expandCapacity > 0) {
            sizeCopy(this.size, expandCapacity);
        }
    }

    private void freeToCache(int[] iArr, Object[] objArr, Object[] objArr2) {
        objArr[1] = iArr;
        objArr[2] = objArr2;
        for (int i = this.size - 1; i >= 3; i--) {
            objArr[i] = null;
        }
        memManager.freeToCache(objArr, iArr.length);
    }

    private int indexOf(Object obj, int i) {
        int i2;
        int binarySearchIntArrayMatchObject;
        if (this.size == 0) {
            return -1;
        }
        do {
            i2 = this.size;
            binarySearchIntArrayMatchObject = ArrayHelpers.binarySearchIntArrayMatchObject(this.hashes, this.keyArray, i2, i, obj);
        } while (i2 != this.size);
        return binarySearchIntArrayMatchObject;
    }

    private final int keyHashCode(Object obj) {
        if (obj == null) {
            return 0;
        }
        return this.useIdentityHash ? System.identityHashCode(obj) : obj.hashCode();
    }

    private void shrinkIfNecessary() {
        int shrinkCapacity = memManager.getShrinkCapacity(this.hashes.length, this.size);
        if (shrinkCapacity > 0) {
            sizeCopy(this.size, shrinkCapacity);
        }
    }

    private void sizeCopy(int i, int i2) {
        int[] iArr = this.hashes;
        Object[] objArr = this.keyArray;
        Object[] objArr2 = this.valueArray;
        alloc(i2);
        if (i > 0) {
            System.arraycopy(iArr, 0, this.hashes, 0, this.size);
            System.arraycopy(objArr, 0, this.keyArray, 0, this.size);
            System.arraycopy(objArr2, 0, this.valueArray, 0, this.size);
            freeToCache(iArr, objArr, objArr2);
        }
    }

    /* access modifiers changed from: package-private */
    public final class SubMembers {
        LightweightMap<K, V>.SubMembers.EntrySet entrySet;
        LightweightMap<K, V>.SubMembers.KeySet keySet;
        LightweightMap<K, V>.SubMembers.ValueSet valueSet;

        SubMembers() {
        }

        public LightweightMap<K, V>.SubMembers.EntrySet getEntrySet() {
            LightweightMap<K, V>.SubMembers.EntrySet entrySet2 = this.entrySet;
            if (entrySet2 != null) {
                return entrySet2;
            }
            LightweightMap<K, V>.SubMembers.EntrySet entrySet3 = new EntrySet();
            this.entrySet = entrySet3;
            return entrySet3;
        }

        public LightweightMap<K, V>.SubMembers.KeySet getKeySet() {
            LightweightMap<K, V>.SubMembers.KeySet keySet2 = this.keySet;
            if (keySet2 != null) {
                return keySet2;
            }
            LightweightMap<K, V>.SubMembers.KeySet keySet3 = new KeySet();
            this.keySet = keySet3;
            return keySet3;
        }

        public LightweightMap<K, V>.SubMembers.ValueSet getValueSet() {
            LightweightMap<K, V>.SubMembers.ValueSet valueSet2 = this.valueSet;
            if (valueSet2 != null) {
                return valueSet2;
            }
            LightweightMap<K, V>.SubMembers.ValueSet valueSet3 = new ValueSet();
            this.valueSet = valueSet3;
            return valueSet3;
        }

        /* access modifiers changed from: package-private */
        public final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
            EntrySet() {
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
            public Iterator<Map.Entry<K, V>> iterator() {
                return new EntryIterator();
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
            public int size() {
                return LightweightMap.this.size;
            }
        }

        /* access modifiers changed from: package-private */
        public final class KeySet extends AbstractSet<K> {
            KeySet() {
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
            public Iterator<K> iterator() {
                return new KeyIterator();
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
            public int size() {
                return LightweightMap.this.size;
            }
        }

        /* access modifiers changed from: package-private */
        public final class ValueSet extends AbstractSet<V> {
            ValueSet() {
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
            public Iterator<V> iterator() {
                return new ValueIterator();
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
            public int size() {
                return LightweightMap.this.size;
            }
        }

        final class KeyIterator implements Iterator<K> {
            int iterIndex = 0;

            KeyIterator() {
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.iterIndex < LightweightMap.this.size;
            }

            @Override // java.util.Iterator
            public K next() {
                if (this.iterIndex < LightweightMap.this.size) {
                    Object[] objArr = LightweightMap.this.keyArray;
                    int i = this.iterIndex;
                    this.iterIndex = i + 1;
                    return (K) objArr[i];
                }
                throw new NoSuchElementException();
            }
        }

        final class ValueIterator implements Iterator<V> {
            int iterIndex = 0;

            ValueIterator() {
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.iterIndex < LightweightMap.this.size;
            }

            @Override // java.util.Iterator
            public V next() {
                if (this.iterIndex < LightweightMap.this.size) {
                    Object[] objArr = LightweightMap.this.valueArray;
                    int i = this.iterIndex;
                    this.iterIndex = i + 1;
                    return (V) objArr[i];
                }
                throw new NoSuchElementException();
            }
        }

        final class EntryIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V> {
            int iterIndex = -1;

            EntryIterator() {
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.iterIndex < LightweightMap.this.size - 1;
            }

            @Override // java.util.Iterator
            public Map.Entry<K, V> next() {
                if (hasNext()) {
                    this.iterIndex++;
                    return this;
                }
                throw new NoSuchElementException();
            }

            @Override // java.util.Map.Entry
            public K getKey() {
                return (K) LightweightMap.this.keyArray[this.iterIndex];
            }

            @Override // java.util.Map.Entry
            public V getValue() {
                return (V) LightweightMap.this.valueArray[this.iterIndex];
            }

            @Override // java.util.Map.Entry
            public V setValue(V v) {
                V v2 = (V) LightweightMap.this.valueArray[this.iterIndex];
                LightweightMap.this.valueArray[this.iterIndex] = v;
                return v2;
            }
        }
    }
}
