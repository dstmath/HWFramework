package com.android.framework.protobuf;

import com.android.framework.protobuf.Internal;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

final class DoubleArrayList extends AbstractProtobufList<Double> implements Internal.DoubleList, RandomAccess {
    private static final DoubleArrayList EMPTY_LIST = new DoubleArrayList();
    private double[] array;
    private int size;

    static {
        EMPTY_LIST.makeImmutable();
    }

    public static DoubleArrayList emptyList() {
        return EMPTY_LIST;
    }

    DoubleArrayList() {
        this(new double[10], 0);
    }

    private DoubleArrayList(double[] array2, int size2) {
        this.array = array2;
        this.size = size2;
    }

    @Override // com.android.framework.protobuf.AbstractProtobufList
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoubleArrayList)) {
            return super.equals(o);
        }
        DoubleArrayList other = (DoubleArrayList) o;
        if (this.size != other.size) {
            return false;
        }
        double[] arr = other.array;
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
            result = (result * 31) + Internal.hashLong(Double.doubleToLongBits(this.array[i]));
        }
        return result;
    }

    /* Return type fixed from 'com.android.framework.protobuf.Internal$DoubleList' to match base method */
    @Override // com.android.framework.protobuf.Internal.DoubleList, com.android.framework.protobuf.Internal.ProtobufList
    public Internal.ProtobufList<Double> mutableCopyWithCapacity(int capacity) {
        if (capacity >= this.size) {
            return new DoubleArrayList(Arrays.copyOf(this.array, capacity), this.size);
        }
        throw new IllegalArgumentException();
    }

    @Override // java.util.List, java.util.AbstractList
    public Double get(int index) {
        return Double.valueOf(getDouble(index));
    }

    @Override // com.android.framework.protobuf.Internal.DoubleList
    public double getDouble(int index) {
        ensureIndexInRange(index);
        return this.array[index];
    }

    public int size() {
        return this.size;
    }

    public Double set(int index, Double element) {
        return Double.valueOf(setDouble(index, element.doubleValue()));
    }

    @Override // com.android.framework.protobuf.Internal.DoubleList
    public double setDouble(int index, double element) {
        ensureIsMutable();
        ensureIndexInRange(index);
        double[] dArr = this.array;
        double previousValue = dArr[index];
        dArr[index] = element;
        return previousValue;
    }

    public void add(int index, Double element) {
        addDouble(index, element.doubleValue());
    }

    @Override // com.android.framework.protobuf.Internal.DoubleList
    public void addDouble(double element) {
        addDouble(this.size, element);
    }

    private void addDouble(int index, double element) {
        int i;
        ensureIsMutable();
        if (index < 0 || index > (i = this.size)) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(index));
        }
        double[] dArr = this.array;
        if (i < dArr.length) {
            System.arraycopy(dArr, index, dArr, index + 1, i - index);
        } else {
            double[] newArray = new double[(((i * 3) / 2) + 1)];
            System.arraycopy(dArr, 0, newArray, 0, index);
            System.arraycopy(this.array, index, newArray, index + 1, this.size - index);
            this.array = newArray;
        }
        this.array[index] = element;
        this.size++;
        this.modCount++;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, com.android.framework.protobuf.AbstractProtobufList
    public boolean addAll(Collection<? extends Double> collection) {
        ensureIsMutable();
        if (collection == null) {
            throw new NullPointerException();
        } else if (!(collection instanceof DoubleArrayList)) {
            return super.addAll(collection);
        } else {
            DoubleArrayList list = (DoubleArrayList) collection;
            int i = list.size;
            if (i == 0) {
                return false;
            }
            int i2 = this.size;
            if (Integer.MAX_VALUE - i2 >= i) {
                int newSize = i2 + i;
                double[] dArr = this.array;
                if (newSize > dArr.length) {
                    this.array = Arrays.copyOf(dArr, newSize);
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
            if (o.equals(Double.valueOf(this.array[i]))) {
                double[] dArr = this.array;
                System.arraycopy(dArr, i + 1, dArr, i, this.size - i);
                this.size--;
                this.modCount++;
                return true;
            }
        }
        return false;
    }

    @Override // java.util.List, java.util.AbstractList, com.android.framework.protobuf.AbstractProtobufList
    public Double remove(int index) {
        ensureIsMutable();
        ensureIndexInRange(index);
        double[] dArr = this.array;
        double value = dArr[index];
        System.arraycopy(dArr, index + 1, dArr, index, this.size - index);
        this.size--;
        this.modCount++;
        return Double.valueOf(value);
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
