package com.google.protobuf;

import com.android.server.wifi.ScoringParams;
import com.google.protobuf.Internal;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

final class LongArrayList extends AbstractProtobufList<Long> implements Internal.LongList, RandomAccess {
    private static final LongArrayList EMPTY_LIST = new LongArrayList();
    private long[] array;
    private int size;

    static {
        EMPTY_LIST.makeImmutable();
    }

    public static LongArrayList emptyList() {
        return EMPTY_LIST;
    }

    LongArrayList() {
        this(new long[10], 0);
    }

    private LongArrayList(long[] array2, int size2) {
        this.array = array2;
        this.size = size2;
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntArrayList)) {
            return super.equals(o);
        }
        LongArrayList other = (LongArrayList) o;
        if (this.size != other.size) {
            return false;
        }
        long[] arr = other.array;
        for (int i = 0; i < this.size; i++) {
            if (this.array[i] != arr[i]) {
                return false;
            }
        }
        return true;
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < this.size; i++) {
            result = (result * 31) + Internal.hashLong(this.array[i]);
        }
        return result;
    }

    @Override // com.google.protobuf.Internal.ProtobufList, com.google.protobuf.Internal.BooleanList
    public Internal.LongList mutableCopyWithCapacity(int capacity) {
        if (capacity >= this.size) {
            return new LongArrayList(Arrays.copyOf(this.array, capacity), this.size);
        }
        throw new IllegalArgumentException();
    }

    @Override // java.util.AbstractList, java.util.List
    public Long get(int index) {
        return Long.valueOf(getLong(index));
    }

    @Override // com.google.protobuf.Internal.LongList
    public long getLong(int index) {
        ensureIndexInRange(index);
        return this.array[index];
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return this.size;
    }

    public Long set(int index, Long element) {
        return Long.valueOf(setLong(index, element.longValue()));
    }

    @Override // com.google.protobuf.Internal.LongList
    public long setLong(int index, long element) {
        ensureIsMutable();
        ensureIndexInRange(index);
        long[] jArr = this.array;
        long previousValue = jArr[index];
        jArr[index] = element;
        return previousValue;
    }

    public void add(int index, Long element) {
        addLong(index, element.longValue());
    }

    @Override // com.google.protobuf.Internal.LongList
    public void addLong(long element) {
        addLong(this.size, element);
    }

    private void addLong(int index, long element) {
        int i;
        ensureIsMutable();
        if (index < 0 || index > (i = this.size)) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(index));
        }
        long[] jArr = this.array;
        if (i < jArr.length) {
            System.arraycopy(jArr, index, jArr, index + 1, i - index);
        } else {
            long[] newArray = new long[(((i * 3) / 2) + 1)];
            System.arraycopy(jArr, 0, newArray, 0, index);
            System.arraycopy(this.array, index, newArray, index + 1, this.size - index);
            this.array = newArray;
        }
        this.array[index] = element;
        this.size++;
        this.modCount++;
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends Long> collection) {
        ensureIsMutable();
        if (collection == null) {
            throw new NullPointerException();
        } else if (!(collection instanceof LongArrayList)) {
            return super.addAll(collection);
        } else {
            LongArrayList list = (LongArrayList) collection;
            int i = list.size;
            if (i == 0) {
                return false;
            }
            int i2 = this.size;
            if (ScoringParams.Values.MAX_EXPID - i2 >= i) {
                int newSize = i2 + i;
                long[] jArr = this.array;
                if (newSize > jArr.length) {
                    this.array = Arrays.copyOf(jArr, newSize);
                }
                System.arraycopy(list.array, 0, this.array, this.size, list.size);
                this.size = newSize;
                this.modCount++;
                return true;
            }
            throw new OutOfMemoryError();
        }
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean remove(Object o) {
        ensureIsMutable();
        for (int i = 0; i < this.size; i++) {
            if (o.equals(Long.valueOf(this.array[i]))) {
                long[] jArr = this.array;
                System.arraycopy(jArr, i + 1, jArr, i, this.size - i);
                this.size--;
                this.modCount++;
                return true;
            }
        }
        return false;
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List
    public Long remove(int index) {
        ensureIsMutable();
        ensureIndexInRange(index);
        long[] jArr = this.array;
        long value = jArr[index];
        System.arraycopy(jArr, index + 1, jArr, index, this.size - index);
        this.size--;
        this.modCount++;
        return Long.valueOf(value);
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
