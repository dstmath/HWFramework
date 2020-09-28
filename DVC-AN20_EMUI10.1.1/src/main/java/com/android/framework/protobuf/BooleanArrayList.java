package com.android.framework.protobuf;

import com.android.framework.protobuf.Internal;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

final class BooleanArrayList extends AbstractProtobufList<Boolean> implements Internal.BooleanList, RandomAccess {
    private static final BooleanArrayList EMPTY_LIST = new BooleanArrayList();
    private boolean[] array;
    private int size;

    static {
        EMPTY_LIST.makeImmutable();
    }

    public static BooleanArrayList emptyList() {
        return EMPTY_LIST;
    }

    BooleanArrayList() {
        this(new boolean[10], 0);
    }

    private BooleanArrayList(boolean[] array2, int size2) {
        this.array = array2;
        this.size = size2;
    }

    @Override // com.android.framework.protobuf.AbstractProtobufList
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BooleanArrayList)) {
            return super.equals(o);
        }
        BooleanArrayList other = (BooleanArrayList) o;
        if (this.size != other.size) {
            return false;
        }
        boolean[] arr = other.array;
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
            result = (result * 31) + Internal.hashBoolean(this.array[i]);
        }
        return result;
    }

    /* Return type fixed from 'com.android.framework.protobuf.Internal$BooleanList' to match base method */
    @Override // com.android.framework.protobuf.Internal.BooleanList, com.android.framework.protobuf.Internal.ProtobufList
    public Internal.ProtobufList<Boolean> mutableCopyWithCapacity(int capacity) {
        if (capacity >= this.size) {
            return new BooleanArrayList(Arrays.copyOf(this.array, capacity), this.size);
        }
        throw new IllegalArgumentException();
    }

    @Override // java.util.List, java.util.AbstractList
    public Boolean get(int index) {
        return Boolean.valueOf(getBoolean(index));
    }

    @Override // com.android.framework.protobuf.Internal.BooleanList
    public boolean getBoolean(int index) {
        ensureIndexInRange(index);
        return this.array[index];
    }

    public int size() {
        return this.size;
    }

    public Boolean set(int index, Boolean element) {
        return Boolean.valueOf(setBoolean(index, element.booleanValue()));
    }

    @Override // com.android.framework.protobuf.Internal.BooleanList
    public boolean setBoolean(int index, boolean element) {
        ensureIsMutable();
        ensureIndexInRange(index);
        boolean[] zArr = this.array;
        boolean previousValue = zArr[index];
        zArr[index] = element;
        return previousValue;
    }

    public void add(int index, Boolean element) {
        addBoolean(index, element.booleanValue());
    }

    @Override // com.android.framework.protobuf.Internal.BooleanList
    public void addBoolean(boolean element) {
        addBoolean(this.size, element);
    }

    private void addBoolean(int index, boolean element) {
        int i;
        ensureIsMutable();
        if (index < 0 || index > (i = this.size)) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(index));
        }
        boolean[] zArr = this.array;
        if (i < zArr.length) {
            System.arraycopy(zArr, index, zArr, index + 1, i - index);
        } else {
            boolean[] newArray = new boolean[(((i * 3) / 2) + 1)];
            System.arraycopy(zArr, 0, newArray, 0, index);
            System.arraycopy(this.array, index, newArray, index + 1, this.size - index);
            this.array = newArray;
        }
        this.array[index] = element;
        this.size++;
        this.modCount++;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, com.android.framework.protobuf.AbstractProtobufList
    public boolean addAll(Collection<? extends Boolean> collection) {
        ensureIsMutable();
        if (collection == null) {
            throw new NullPointerException();
        } else if (!(collection instanceof BooleanArrayList)) {
            return super.addAll(collection);
        } else {
            BooleanArrayList list = (BooleanArrayList) collection;
            int i = list.size;
            if (i == 0) {
                return false;
            }
            int i2 = this.size;
            if (Integer.MAX_VALUE - i2 >= i) {
                int newSize = i2 + i;
                boolean[] zArr = this.array;
                if (newSize > zArr.length) {
                    this.array = Arrays.copyOf(zArr, newSize);
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
            if (o.equals(Boolean.valueOf(this.array[i]))) {
                boolean[] zArr = this.array;
                System.arraycopy(zArr, i + 1, zArr, i, this.size - i);
                this.size--;
                this.modCount++;
                return true;
            }
        }
        return false;
    }

    @Override // java.util.List, java.util.AbstractList, com.android.framework.protobuf.AbstractProtobufList
    public Boolean remove(int index) {
        ensureIsMutable();
        ensureIndexInRange(index);
        boolean[] zArr = this.array;
        boolean value = zArr[index];
        System.arraycopy(zArr, index + 1, zArr, index, this.size - index);
        this.size--;
        this.modCount++;
        return Boolean.valueOf(value);
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
