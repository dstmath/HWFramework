package com.android.framework.protobuf;

import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public final class ProtobufArrayList<E> extends AbstractProtobufList<E> {
    private static final ProtobufArrayList<Object> EMPTY_LIST = new ProtobufArrayList<>();
    private final List<E> list;

    static {
        EMPTY_LIST.makeImmutable();
    }

    public static <E> ProtobufArrayList<E> emptyList() {
        return (ProtobufArrayList<E>) EMPTY_LIST;
    }

    ProtobufArrayList() {
        this(new ArrayList(10));
    }

    private ProtobufArrayList(List<E> list2) {
        this.list = list2;
    }

    @Override // com.android.framework.protobuf.Internal.ProtobufList
    public ProtobufArrayList<E> mutableCopyWithCapacity(int capacity) {
        if (capacity >= size()) {
            List<E> newList = new ArrayList<>(capacity);
            newList.addAll(this.list);
            return new ProtobufArrayList<>(newList);
        }
        throw new IllegalArgumentException();
    }

    @Override // java.util.List, java.util.AbstractList, com.android.framework.protobuf.AbstractProtobufList
    public void add(int index, E element) {
        ensureIsMutable();
        this.list.add(index, element);
        this.modCount++;
    }

    @Override // java.util.List, java.util.AbstractList
    public E get(int index) {
        return this.list.get(index);
    }

    @Override // java.util.List, java.util.AbstractList, com.android.framework.protobuf.AbstractProtobufList
    public E remove(int index) {
        ensureIsMutable();
        E toReturn = this.list.remove(index);
        this.modCount++;
        return toReturn;
    }

    @Override // java.util.List, java.util.AbstractList, com.android.framework.protobuf.AbstractProtobufList
    public E set(int index, E element) {
        ensureIsMutable();
        E toReturn = this.list.set(index, element);
        this.modCount++;
        return toReturn;
    }

    public int size() {
        return this.list.size();
    }
}
