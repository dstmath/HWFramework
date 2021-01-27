package ohos.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public final class LightweightSet<E> implements Collection<E>, Set<E> {
    private static ArrayMemManager memManager = new ArrayMemManager();
    private int[] hashes;
    private Object[] keyArray;
    private int size;
    private boolean useIdentityHash;

    public LightweightSet() {
        this(0, false);
    }

    public LightweightSet(int i) {
        this(i, false);
    }

    public LightweightSet(int i, boolean z) {
        this.hashes = EmptyArray.INT;
        this.keyArray = EmptyArray.OBJECT;
        this.useIdentityHash = z;
        alloc(i);
    }

    public LightweightSet(LightweightSet<? extends E> lightweightSet) {
        this(lightweightSet.size, lightweightSet.useIdentityHash);
        if (!lightweightSet.isEmpty()) {
            addAll((LightweightSet) lightweightSet);
        }
    }

    @Override // java.util.Collection, java.util.Set
    public boolean add(E e) {
        int indexOf = indexOf(e);
        if (indexOf >= 0) {
            return false;
        }
        int i = ~indexOf;
        expandIfNecessary();
        int i2 = this.size;
        if (i < i2) {
            int[] iArr = this.hashes;
            int i3 = i + 1;
            System.arraycopy(iArr, i, iArr, i3, i2 - i);
            Object[] objArr = this.keyArray;
            System.arraycopy(objArr, i, objArr, i3, this.size - i);
        }
        this.hashes[i] = keyHashCode(e);
        this.keyArray[i] = e;
        this.size++;
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: ohos.utils.LightweightSet<E> */
    /* JADX WARN: Multi-variable type inference failed */
    public boolean addAll(LightweightSet<? extends E> lightweightSet) {
        int size2 = lightweightSet.size();
        ensureCapacity(this.size + size2);
        if (this.size == 0) {
            System.arraycopy(lightweightSet.hashes, 0, this.hashes, 0, size2);
            System.arraycopy(lightweightSet.keyArray, 0, this.keyArray, 0, size2);
            this.size = size2;
            return true;
        }
        boolean z = false;
        for (int i = 0; i < lightweightSet.size; i++) {
            z |= add(lightweightSet.valueAt(i));
        }
        return z;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: ohos.utils.LightweightSet<E> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Collection, java.util.Set
    public boolean addAll(Collection<? extends E> collection) {
        ensureCapacity(this.size + collection.size());
        Iterator<? extends E> it = collection.iterator();
        boolean z = false;
        while (it.hasNext()) {
            z |= add(it.next());
        }
        return z;
    }

    @Override // java.util.Collection, java.util.Set
    public void clear() {
        if (this.size != 0) {
            freeToCache(this.hashes, this.keyArray);
            this.hashes = EmptyArray.INT;
            this.keyArray = EmptyArray.OBJECT;
            this.size = 0;
        }
    }

    @Override // java.util.Collection, java.util.Set
    public boolean contains(Object obj) {
        return indexOf(obj) >= 0;
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

    public void ensureCapacity(int i) {
        if (this.hashes.length < i) {
            sizeCopy(this.size, i);
        }
    }

    @Override // java.util.Collection, java.lang.Object, java.util.Set
    public boolean equals(Object obj) {
        if (obj != this && (obj instanceof Set)) {
            Set set = (Set) obj;
            if (set.size() != this.size) {
                return false;
            }
            for (int i = 0; i < this.size; i++) {
                try {
                    if (!set.contains(valueAt(i))) {
                        return false;
                    }
                } catch (ClassCastException | NullPointerException unused) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override // java.util.Collection, java.lang.Object, java.util.Set
    public int hashCode() {
        int[] iArr = this.hashes;
        int i = 0;
        for (int i2 = 0; i2 < this.size; i2++) {
            i += iArr[i2];
        }
        return i;
    }

    public int indexOf(Object obj) {
        return indexOf(obj, keyHashCode(obj));
    }

    @Override // java.util.Collection, java.util.Set
    public boolean isEmpty() {
        return this.size <= 0;
    }

    @Override // java.util.Collection, java.lang.Iterable, java.util.Set
    public Iterator<E> iterator() {
        return new KeyIterator();
    }

    @Override // java.util.Collection, java.util.Set
    public boolean remove(Object obj) {
        int indexOf = indexOf(obj);
        if (indexOf < 0) {
            return false;
        }
        removeAt(indexOf);
        return true;
    }

    public boolean removeAll(LightweightSet<? extends E> lightweightSet) {
        boolean z = false;
        for (int size2 = lightweightSet.size(); size2 >= 0; size2--) {
            z |= remove(lightweightSet.valueAt(size2));
        }
        return z;
    }

    @Override // java.util.Collection, java.util.Set
    public boolean removeAll(Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        boolean z = false;
        while (it.hasNext()) {
            z |= remove(it.next());
        }
        return z;
    }

    public E removeAt(int i) {
        if (i < 0) {
            i += this.size;
        }
        int i2 = this.size;
        if (i >= i2 || i < 0) {
            return null;
        }
        E e = (E) this.keyArray[i];
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
            }
            this.keyArray[this.size] = null;
        }
        return e;
    }

    @Override // java.util.Collection, java.util.Set
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

    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.lang.Object
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.size; i++) {
            Object[] objArr = this.keyArray;
            arrayList.add(objArr[i] == null ? "null" : objArr[i].toString());
        }
        return "{" + String.join(", ", arrayList) + "}";
    }

    @Override // java.util.Collection, java.util.Set
    public int size() {
        return this.size;
    }

    @Override // java.util.Collection, java.util.Set
    public Object[] toArray() {
        int i = this.size;
        Object[] objArr = new Object[i];
        System.arraycopy(this.keyArray, 0, objArr, 0, i);
        return objArr;
    }

    @Override // java.util.Collection, java.util.Set
    public <T> T[] toArray(T[] tArr) {
        if (tArr.length < this.size) {
            tArr = (T[]) ((Object[]) Array.newInstance(tArr.getClass().getComponentType(), this.size));
        }
        System.arraycopy(this.keyArray, 0, tArr, 0, this.size);
        return tArr;
    }

    public E valueAt(int i) {
        Object[] objArr = this.keyArray;
        if (i < 0) {
            i += this.size;
        }
        return (E) objArr[i];
    }

    public E valueAt(int i, E e) {
        if (i < 0) {
            i += this.size;
        }
        return (i < 0 || i >= this.size) ? e : (E) this.keyArray[i];
    }

    private void sizeCopy(int i, int i2) {
        int[] iArr = this.hashes;
        Object[] objArr = this.keyArray;
        alloc(i2);
        if (i > 0) {
            System.arraycopy(iArr, 0, this.hashes, 0, this.size);
            System.arraycopy(objArr, 0, this.keyArray, 0, this.size);
            freeToCache(iArr, objArr);
        }
    }

    private void alloc(int i) {
        if (i == 0) {
            this.hashes = EmptyArray.INT;
            this.keyArray = EmptyArray.OBJECT;
            this.size = 0;
            return;
        }
        Object[] allocFromCache = memManager.allocFromCache(i);
        if (allocFromCache != null) {
            this.keyArray = allocFromCache;
            this.hashes = (int[]) this.keyArray[1];
            return;
        }
        this.hashes = new int[i];
        this.keyArray = new Object[i];
    }

    private void expandIfNecessary() {
        int expandCapacity = memManager.getExpandCapacity(this.hashes.length, this.size);
        if (expandCapacity > 0) {
            sizeCopy(this.size, expandCapacity);
        }
    }

    private void freeToCache(int[] iArr, Object[] objArr) {
        objArr[1] = iArr;
        for (int i = this.size - 1; i >= 2; i--) {
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

    private int keyHashCode(Object obj) {
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

    final class KeyIterator<V> implements Iterator<V> {
        private int iterIndex = 0;

        KeyIterator() {
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.iterIndex < LightweightSet.this.size;
        }

        @Override // java.util.Iterator
        public V next() {
            if (this.iterIndex < LightweightSet.this.size) {
                Object[] objArr = LightweightSet.this.keyArray;
                int i = this.iterIndex;
                this.iterIndex = i + 1;
                return (V) objArr[i];
            }
            throw new NoSuchElementException();
        }
    }
}
