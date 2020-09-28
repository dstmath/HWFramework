package com.android.framework.protobuf;

import com.android.framework.protobuf.Internal;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

final class IntArrayList extends AbstractProtobufList<Integer> implements Internal.IntList, RandomAccess {
    private static final IntArrayList EMPTY_LIST = new IntArrayList();
    private int[] array;
    private int size;

    static {
        EMPTY_LIST.makeImmutable();
    }

    public static IntArrayList emptyList() {
        return EMPTY_LIST;
    }

    IntArrayList() {
        this(new int[10], 0);
    }

    private IntArrayList(int[] array2, int size2) {
        this.array = array2;
        this.size = size2;
    }

    @Override // com.android.framework.protobuf.AbstractProtobufList
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntArrayList)) {
            return super.equals(o);
        }
        IntArrayList other = (IntArrayList) o;
        if (this.size != other.size) {
            return false;
        }
        int[] arr = other.array;
        for (int i = 0; i < this.size; i++) {
            if (this.array[i] != arr[i]) {
                return false;
            }
        }
        return true;
    }

    @Override // com.android.framework.protobuf.AbstractProtobufList
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < this.size; i++) {
            result = (result * 31) + this.array[i];
        }
        return result;
    }

    /* Return type fixed from 'com.android.framework.protobuf.Internal$IntList' to match base method */
    @Override // com.android.framework.protobuf.Internal.IntList, com.android.framework.protobuf.Internal.ProtobufList
    public Internal.ProtobufList<Integer> mutableCopyWithCapacity(int capacity) {
        if (capacity >= this.size) {
            return new IntArrayList(Arrays.copyOf(this.array, capacity), this.size);
        }
        throw new IllegalArgumentException();
    }

    @Override // java.util.List, java.util.AbstractList
    public Integer get(int index) {
        return Integer.valueOf(getInt(index));
    }

    @Override // com.android.framework.protobuf.Internal.IntList
    public int getInt(int index) {
        ensureIndexInRange(index);
        return this.array[index];
    }

    public int size() {
        return this.size;
    }

    public Integer set(int index, Integer element) {
        return Integer.valueOf(setInt(index, element.intValue()));
    }

    @Override // com.android.framework.protobuf.Internal.IntList
    public int setInt(int index, int element) {
        ensureIsMutable();
        ensureIndexInRange(index);
        int[] iArr = this.array;
        int previousValue = iArr[index];
        iArr[index] = element;
        return previousValue;
    }

    public void add(int index, Integer element) {
        addInt(index, element.intValue());
    }

    @Override // com.android.framework.protobuf.Internal.IntList
    public void addInt(int element) {
        addInt(this.size, element);
    }

    private void addInt(int index, int element) {
        int i;
        ensureIsMutable();
        if (index < 0 || index > (i = this.size)) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(index));
        }
        int[] iArr = this.array;
        if (i < iArr.length) {
            System.arraycopy(iArr, index, iArr, index + 1, i - index);
        } else {
            int[] newArray = new int[(((i * 3) / 2) + 1)];
            System.arraycopy(iArr, 0, newArray, 0, index);
            System.arraycopy(this.array, index, newArray, index + 1, this.size - index);
            this.array = newArray;
        }
        this.array[index] = element;
        this.size++;
        this.modCount++;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, com.android.framework.protobuf.AbstractProtobufList
    public boolean addAll(Collection<? extends Integer> collection) {
        ensureIsMutable();
        if (collection == null) {
            throw new NullPointerException();
        } else if (!(collection instanceof IntArrayList)) {
            return super.addAll(collection);
        } else {
            IntArrayList list = (IntArrayList) collection;
            int i = list.size;
            if (i == 0) {
                return false;
            }
            int i2 = this.size;
            if (Integer.MAX_VALUE - i2 >= i) {
                int newSize = i2 + i;
                int[] iArr = this.array;
                if (newSize > iArr.length) {
                    this.array = Arrays.copyOf(iArr, newSize);
                }
                System.arraycopy(list.array, 0, this.array, this.size, list.size);
                this.size = newSize;
                this.modCount++;
                return true;
            }
            throw new OutOfMemoryError();
        }
    }

    @Override // java.util.List, com.android.framework.protobuf.AbstractProtobufList
    public boolean remove(Object o) {
        ensureIsMutable();
        for (int i = 0; i < this.size; i++) {
            if (o.equals(Integer.valueOf(this.array[i]))) {
                int[] iArr = this.array;
                System.arraycopy(iArr, i + 1, iArr, i, this.size - i);
                this.size--;
                this.modCount++;
                return true;
            }
        }
        return false;
    }

    @Override // java.util.List, java.util.AbstractList, com.android.framework.protobuf.AbstractProtobufList
    public Integer remove(int index) {
        ensureIsMutable();
        ensureIndexInRange(index);
        int[] iArr = this.array;
        int value = iArr[index];
        System.arraycopy(iArr, index + 1, iArr, index, this.size - index);
        this.size--;
        this.modCount++;
        return Integer.valueOf(value);
    }

    private void ensureIndexInRange(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(index));
        }
    }

    private String makeOutOfBoundsExceptionMessage(int index) {
        return "Index:" + index + ", Size:" + this.size;
    }
}
