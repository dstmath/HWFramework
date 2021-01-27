package com.google.protobuf;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class LazyStringArrayList extends AbstractProtobufList<String> implements LazyStringList, RandomAccess {
    public static final LazyStringList EMPTY = EMPTY_LIST;
    private static final LazyStringArrayList EMPTY_LIST = new LazyStringArrayList();
    private final List<Object> list;

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List, java.util.Collection, java.lang.Object
    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    @Override // com.google.protobuf.AbstractProtobufList, com.google.protobuf.Internal.ProtobufList
    public /* bridge */ /* synthetic */ boolean isModifiable() {
        return super.isModifiable();
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public /* bridge */ /* synthetic */ boolean remove(Object obj) {
        return super.remove(obj);
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public /* bridge */ /* synthetic */ boolean removeAll(Collection collection) {
        return super.removeAll(collection);
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public /* bridge */ /* synthetic */ boolean retainAll(Collection collection) {
        return super.retainAll(collection);
    }

    static {
        EMPTY_LIST.makeImmutable();
    }

    static LazyStringArrayList emptyList() {
        return EMPTY_LIST;
    }

    public LazyStringArrayList() {
        this(10);
    }

    public LazyStringArrayList(int intialCapacity) {
        this((ArrayList<Object>) new ArrayList(intialCapacity));
    }

    public LazyStringArrayList(LazyStringList from) {
        this.list = new ArrayList(from.size());
        addAll(from);
    }

    public LazyStringArrayList(List<String> from) {
        this((ArrayList<Object>) new ArrayList(from));
    }

    private LazyStringArrayList(ArrayList<Object> list2) {
        this.list = list2;
    }

    @Override // com.google.protobuf.Internal.ProtobufList, com.google.protobuf.Internal.BooleanList
    public LazyStringArrayList mutableCopyWithCapacity(int capacity) {
        if (capacity >= size()) {
            ArrayList<Object> newList = new ArrayList<>(capacity);
            newList.addAll(this.list);
            return new LazyStringArrayList(newList);
        }
        throw new IllegalArgumentException();
    }

    @Override // java.util.AbstractList, java.util.List
    public String get(int index) {
        Object o = this.list.get(index);
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof ByteString) {
            ByteString bs = (ByteString) o;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.list.set(index, s);
            }
            return s;
        }
        byte[] ba = (byte[]) o;
        String s2 = Internal.toStringUtf8(ba);
        if (Internal.isValidUtf8(ba)) {
            this.list.set(index, s2);
        }
        return s2;
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return this.list.size();
    }

    public String set(int index, String s) {
        ensureIsMutable();
        return asString(this.list.set(index, s));
    }

    public void add(int index, String element) {
        ensureIsMutable();
        this.list.add(index, element);
        this.modCount++;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void add(int index, ByteString element) {
        ensureIsMutable();
        this.list.add(index, element);
        this.modCount++;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void add(int index, byte[] element) {
        ensureIsMutable();
        this.list.add(index, element);
        this.modCount++;
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends String> c) {
        return addAll(size(), c);
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List
    public boolean addAll(int index, Collection<? extends String> c) {
        ensureIsMutable();
        boolean ret = this.list.addAll(index, c instanceof LazyStringList ? ((LazyStringList) c).getUnderlyingElements() : c);
        this.modCount++;
        return ret;
    }

    @Override // com.google.protobuf.LazyStringList
    public boolean addAllByteString(Collection<? extends ByteString> values) {
        ensureIsMutable();
        boolean ret = this.list.addAll(values);
        this.modCount++;
        return ret;
    }

    @Override // com.google.protobuf.LazyStringList
    public boolean addAllByteArray(Collection<byte[]> c) {
        ensureIsMutable();
        boolean ret = this.list.addAll(c);
        this.modCount++;
        return ret;
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.List
    public String remove(int index) {
        ensureIsMutable();
        Object o = this.list.remove(index);
        this.modCount++;
        return asString(o);
    }

    @Override // com.google.protobuf.AbstractProtobufList, java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        ensureIsMutable();
        this.list.clear();
        this.modCount++;
    }

    @Override // com.google.protobuf.LazyStringList
    public void add(ByteString element) {
        ensureIsMutable();
        this.list.add(element);
        this.modCount++;
    }

    @Override // com.google.protobuf.LazyStringList
    public void add(byte[] element) {
        ensureIsMutable();
        this.list.add(element);
        this.modCount++;
    }

    @Override // com.google.protobuf.LazyStringList
    public Object getRaw(int index) {
        return this.list.get(index);
    }

    @Override // com.google.protobuf.LazyStringList
    public ByteString getByteString(int index) {
        Object o = this.list.get(index);
        ByteString b = asByteString(o);
        if (b != o) {
            this.list.set(index, b);
        }
        return b;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: byte[] */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.protobuf.LazyStringList
    public byte[] getByteArray(int index) {
        Object o = this.list.get(index);
        byte[] b = asByteArray(o);
        if (b != o) {
            this.list.set(index, b);
        }
        return b;
    }

    @Override // com.google.protobuf.LazyStringList
    public void set(int index, ByteString s) {
        setAndReturn(index, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Object setAndReturn(int index, ByteString s) {
        ensureIsMutable();
        return this.list.set(index, s);
    }

    @Override // com.google.protobuf.LazyStringList
    public void set(int index, byte[] s) {
        setAndReturn(index, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Object setAndReturn(int index, byte[] s) {
        ensureIsMutable();
        return this.list.set(index, s);
    }

    private static String asString(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof ByteString) {
            return ((ByteString) o).toStringUtf8();
        }
        return Internal.toStringUtf8((byte[]) o);
    }

    /* access modifiers changed from: private */
    public static ByteString asByteString(Object o) {
        if (o instanceof ByteString) {
            return (ByteString) o;
        }
        if (o instanceof String) {
            return ByteString.copyFromUtf8((String) o);
        }
        return ByteString.copyFrom((byte[]) o);
    }

    /* access modifiers changed from: private */
    public static byte[] asByteArray(Object o) {
        if (o instanceof byte[]) {
            return (byte[]) o;
        }
        if (o instanceof String) {
            return Internal.toByteArray((String) o);
        }
        return ((ByteString) o).toByteArray();
    }

    @Override // com.google.protobuf.LazyStringList
    public List<?> getUnderlyingElements() {
        return Collections.unmodifiableList(this.list);
    }

    @Override // com.google.protobuf.LazyStringList
    public void mergeFrom(LazyStringList other) {
        ensureIsMutable();
        for (Object o : other.getUnderlyingElements()) {
            if (o instanceof byte[]) {
                byte[] b = (byte[]) o;
                this.list.add(Arrays.copyOf(b, b.length));
            } else {
                this.list.add(o);
            }
        }
    }

    private static class ByteArrayListView extends AbstractList<byte[]> implements RandomAccess {
        private final LazyStringArrayList list;

        ByteArrayListView(LazyStringArrayList list2) {
            this.list = list2;
        }

        @Override // java.util.AbstractList, java.util.List
        public byte[] get(int index) {
            return this.list.getByteArray(index);
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
        public int size() {
            return this.list.size();
        }

        public byte[] set(int index, byte[] s) {
            Object o = this.list.setAndReturn(index, s);
            this.modCount++;
            return LazyStringArrayList.asByteArray(o);
        }

        public void add(int index, byte[] s) {
            this.list.add(index, s);
            this.modCount++;
        }

        @Override // java.util.AbstractList, java.util.List
        public byte[] remove(int index) {
            Object o = this.list.remove(index);
            this.modCount++;
            return LazyStringArrayList.asByteArray(o);
        }
    }

    @Override // com.google.protobuf.LazyStringList
    public List<byte[]> asByteArrayList() {
        return new ByteArrayListView(this);
    }

    private static class ByteStringListView extends AbstractList<ByteString> implements RandomAccess {
        private final LazyStringArrayList list;

        ByteStringListView(LazyStringArrayList list2) {
            this.list = list2;
        }

        @Override // java.util.AbstractList, java.util.List
        public ByteString get(int index) {
            return this.list.getByteString(index);
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
        public int size() {
            return this.list.size();
        }

        public ByteString set(int index, ByteString s) {
            Object o = this.list.setAndReturn(index, s);
            this.modCount++;
            return LazyStringArrayList.asByteString(o);
        }

        public void add(int index, ByteString s) {
            this.list.add(index, s);
            this.modCount++;
        }

        @Override // java.util.AbstractList, java.util.List
        public ByteString remove(int index) {
            Object o = this.list.remove(index);
            this.modCount++;
            return LazyStringArrayList.asByteString(o);
        }
    }

    @Override // com.google.protobuf.ProtocolStringList
    public List<ByteString> asByteStringList() {
        return new ByteStringListView(this);
    }

    @Override // com.google.protobuf.LazyStringList
    public LazyStringList getUnmodifiableView() {
        if (isModifiable()) {
            return new UnmodifiableLazyStringList(this);
        }
        return this;
    }
}
