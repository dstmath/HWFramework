package com.google.protobuf;

import com.google.protobuf.Internal;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

abstract class AbstractProtobufList<E> extends AbstractList<E> implements Internal.ProtobufList<E> {
    protected static final int DEFAULT_CAPACITY = 10;
    private boolean isMutable = true;

    AbstractProtobufList() {
    }

    @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        if (!(o instanceof RandomAccess)) {
            return super.equals(o);
        }
        List<?> other = (List) o;
        int size = size();
        if (size != other.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override // java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public int hashCode() {
        int size = size();
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            hashCode = (hashCode * 31) + get(i).hashCode();
        }
        return hashCode;
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean add(E e) {
        ensureIsMutable();
        return super.add(e);
    }

    @Override // java.util.AbstractList, java.util.List
    public void add(int index, E element) {
        ensureIsMutable();
        super.add(index, element);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends E> c) {
        ensureIsMutable();
        return super.addAll(c);
    }

    @Override // java.util.AbstractList, java.util.List
    public boolean addAll(int index, Collection<? extends E> c) {
        ensureIsMutable();
        return super.addAll(index, c);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        ensureIsMutable();
        super.clear();
    }

    @Override // com.google.protobuf.Internal.ProtobufList
    public boolean isModifiable() {
        return this.isMutable;
    }

    @Override // com.google.protobuf.Internal.ProtobufList
    public final void makeImmutable() {
        this.isMutable = false;
    }

    @Override // java.util.AbstractList, java.util.List
    public E remove(int index) {
        ensureIsMutable();
        return (E) super.remove(index);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean remove(Object o) {
        ensureIsMutable();
        return super.remove(o);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean removeAll(Collection<?> c) {
        ensureIsMutable();
        return super.removeAll(c);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean retainAll(Collection<?> c) {
        ensureIsMutable();
        return super.retainAll(c);
    }

    @Override // java.util.AbstractList, java.util.List
    public E set(int index, E element) {
        ensureIsMutable();
        return (E) super.set(index, element);
    }

    /* access modifiers changed from: protected */
    public void ensureIsMutable() {
        if (!this.isMutable) {
            throw new UnsupportedOperationException();
        }
    }
}
