package com.android.framework.protobuf;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

public class UnmodifiableLazyStringList extends AbstractList<String> implements LazyStringList, RandomAccess {
    private final LazyStringList list;

    public UnmodifiableLazyStringList(LazyStringList list2) {
        this.list = list2;
    }

    @Override // java.util.List, java.util.AbstractList
    public String get(int index) {
        return (String) this.list.get(index);
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public Object getRaw(int index) {
        return this.list.getRaw(index);
    }

    public int size() {
        return this.list.size();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public ByteString getByteString(int index) {
        return this.list.getByteString(index);
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public void add(ByteString element) {
        throw new UnsupportedOperationException();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public void set(int index, ByteString element) {
        throw new UnsupportedOperationException();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public boolean addAllByteString(Collection<? extends ByteString> collection) {
        throw new UnsupportedOperationException();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public byte[] getByteArray(int index) {
        return this.list.getByteArray(index);
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public void add(byte[] element) {
        throw new UnsupportedOperationException();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public void set(int index, byte[] element) {
        throw new UnsupportedOperationException();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public boolean addAllByteArray(Collection<byte[]> collection) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.List, java.util.AbstractList
    public ListIterator<String> listIterator(final int index) {
        return new ListIterator<String>() {
            /* class com.android.framework.protobuf.UnmodifiableLazyStringList.AnonymousClass1 */
            ListIterator<String> iter = UnmodifiableLazyStringList.this.list.listIterator(index);

            public boolean hasNext() {
                return this.iter.hasNext();
            }

            @Override // java.util.Iterator, java.util.ListIterator
            public String next() {
                return this.iter.next();
            }

            public boolean hasPrevious() {
                return this.iter.hasPrevious();
            }

            @Override // java.util.ListIterator
            public String previous() {
                return this.iter.previous();
            }

            public int nextIndex() {
                return this.iter.nextIndex();
            }

            public int previousIndex() {
                return this.iter.previousIndex();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(String o) {
                throw new UnsupportedOperationException();
            }

            public void add(String o) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, java.util.AbstractList, java.lang.Iterable
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            /* class com.android.framework.protobuf.UnmodifiableLazyStringList.AnonymousClass2 */
            Iterator<String> iter = UnmodifiableLazyStringList.this.list.iterator();

            public boolean hasNext() {
                return this.iter.hasNext();
            }

            @Override // java.util.Iterator
            public String next() {
                return this.iter.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public List<?> getUnderlyingElements() {
        return this.list.getUnderlyingElements();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public void mergeFrom(LazyStringList other) {
        throw new UnsupportedOperationException();
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public List<byte[]> asByteArrayList() {
        return Collections.unmodifiableList(this.list.asByteArrayList());
    }

    @Override // com.android.framework.protobuf.ProtocolStringList
    public List<ByteString> asByteStringList() {
        return Collections.unmodifiableList(this.list.asByteStringList());
    }

    @Override // com.android.framework.protobuf.LazyStringList
    public LazyStringList getUnmodifiableView() {
        return this;
    }
}
