package com.android.framework.protobuf;

import com.android.internal.logging.nano.MetricsProto;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

public final class Internal {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(EMPTY_BYTE_ARRAY);
    public static final CodedInputStream EMPTY_CODED_INPUT_STREAM = CodedInputStream.newInstance(EMPTY_BYTE_ARRAY);
    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    static final Charset UTF_8 = Charset.forName("UTF-8");

    public interface BooleanList extends ProtobufList<Boolean> {
        void addBoolean(boolean z);

        boolean getBoolean(int i);

        BooleanList mutableCopyWithCapacity(int i);

        boolean setBoolean(int i, boolean z);
    }

    public interface DoubleList extends ProtobufList<Double> {
        void addDouble(double d);

        double getDouble(int i);

        @Override // com.android.framework.protobuf.Internal.ProtobufList, com.android.framework.protobuf.Internal.BooleanList
        DoubleList mutableCopyWithCapacity(int i);

        double setDouble(int i, double d);
    }

    public interface EnumLite {
        int getNumber();
    }

    public interface EnumLiteMap<T extends EnumLite> {
        T findValueByNumber(int i);
    }

    public interface FloatList extends ProtobufList<Float> {
        void addFloat(float f);

        float getFloat(int i);

        @Override // com.android.framework.protobuf.Internal.ProtobufList, com.android.framework.protobuf.Internal.BooleanList
        FloatList mutableCopyWithCapacity(int i);

        float setFloat(int i, float f);
    }

    public interface IntList extends ProtobufList<Integer> {
        void addInt(int i);

        int getInt(int i);

        @Override // com.android.framework.protobuf.Internal.ProtobufList, com.android.framework.protobuf.Internal.BooleanList
        IntList mutableCopyWithCapacity(int i);

        int setInt(int i, int i2);
    }

    public interface LongList extends ProtobufList<Long> {
        void addLong(long j);

        long getLong(int i);

        @Override // com.android.framework.protobuf.Internal.ProtobufList, com.android.framework.protobuf.Internal.BooleanList
        LongList mutableCopyWithCapacity(int i);

        long setLong(int i, long j);
    }

    public interface ProtobufList<E> extends List<E>, RandomAccess {
        boolean isModifiable();

        void makeImmutable();

        @Override // com.android.framework.protobuf.Internal.BooleanList
        ProtobufList<E> mutableCopyWithCapacity(int i);
    }

    private Internal() {
    }

    public static String stringDefaultValue(String bytes) {
        return new String(bytes.getBytes(ISO_8859_1), UTF_8);
    }

    public static ByteString bytesDefaultValue(String bytes) {
        return ByteString.copyFrom(bytes.getBytes(ISO_8859_1));
    }

    public static byte[] byteArrayDefaultValue(String bytes) {
        return bytes.getBytes(ISO_8859_1);
    }

    public static ByteBuffer byteBufferDefaultValue(String bytes) {
        return ByteBuffer.wrap(byteArrayDefaultValue(bytes));
    }

    public static ByteBuffer copyByteBuffer(ByteBuffer source) {
        ByteBuffer temp = source.duplicate();
        temp.clear();
        ByteBuffer result = ByteBuffer.allocate(temp.capacity());
        result.put(temp);
        result.clear();
        return result;
    }

    public static boolean isValidUtf8(ByteString byteString) {
        return byteString.isValidUtf8();
    }

    public static boolean isValidUtf8(byte[] byteArray) {
        return Utf8.isValidUtf8(byteArray);
    }

    public static byte[] toByteArray(String value) {
        return value.getBytes(UTF_8);
    }

    public static String toStringUtf8(byte[] bytes) {
        return new String(bytes, UTF_8);
    }

    public static int hashLong(long n) {
        return (int) ((n >>> 32) ^ n);
    }

    public static int hashBoolean(boolean b) {
        return b ? MetricsProto.MetricsEvent.AUTOFILL_SERVICE_DISABLED_APP : MetricsProto.MetricsEvent.ANOMALY_TYPE_UNOPTIMIZED_BT;
    }

    public static int hashEnum(EnumLite e) {
        return e.getNumber();
    }

    public static int hashEnumList(List<? extends EnumLite> list) {
        int hash = 1;
        for (EnumLite e : list) {
            hash = (hash * 31) + hashEnum(e);
        }
        return hash;
    }

    public static boolean equals(List<byte[]> a, List<byte[]> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Arrays.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static int hashCode(List<byte[]> list) {
        int hash = 1;
        for (byte[] bytes : list) {
            hash = (hash * 31) + hashCode(bytes);
        }
        return hash;
    }

    public static int hashCode(byte[] bytes) {
        return hashCode(bytes, 0, bytes.length);
    }

    static int hashCode(byte[] bytes, int offset, int length) {
        int h = partialHash(length, bytes, offset, length);
        if (h == 0) {
            return 1;
        }
        return h;
    }

    static int partialHash(int h, byte[] bytes, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            h = (h * 31) + bytes[i];
        }
        return h;
    }

    public static boolean equalsByteBuffer(ByteBuffer a, ByteBuffer b) {
        if (a.capacity() != b.capacity()) {
            return false;
        }
        return a.duplicate().clear().equals(b.duplicate().clear());
    }

    public static boolean equalsByteBuffer(List<ByteBuffer> a, List<ByteBuffer> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!equalsByteBuffer(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static int hashCodeByteBuffer(List<ByteBuffer> list) {
        int hash = 1;
        for (ByteBuffer bytes : list) {
            hash = (hash * 31) + hashCodeByteBuffer(bytes);
        }
        return hash;
    }

    public static int hashCodeByteBuffer(ByteBuffer bytes) {
        if (bytes.hasArray()) {
            int h = partialHash(bytes.capacity(), bytes.array(), bytes.arrayOffset(), bytes.capacity());
            if (h == 0) {
                return 1;
            }
            return h;
        }
        int bufferSize = 4096;
        if (bytes.capacity() <= 4096) {
            bufferSize = bytes.capacity();
        }
        byte[] buffer = new byte[bufferSize];
        ByteBuffer duplicated = bytes.duplicate();
        duplicated.clear();
        int h2 = bytes.capacity();
        while (duplicated.remaining() > 0) {
            int length = duplicated.remaining() <= bufferSize ? duplicated.remaining() : bufferSize;
            duplicated.get(buffer, 0, length);
            h2 = partialHash(h2, buffer, 0, length);
        }
        if (h2 == 0) {
            return 1;
        }
        return h2;
    }

    public static <T extends MessageLite> T getDefaultInstance(Class<T> clazz) {
        try {
            Method method = clazz.getMethod("getDefaultInstance", new Class[0]);
            return (T) ((MessageLite) method.invoke(method, new Object[0]));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get default instance for " + clazz, e);
        }
    }

    public static class ListAdapter<F, T> extends AbstractList<T> {
        private final Converter<F, T> converter;
        private final List<F> fromList;

        public interface Converter<F, T> {
            T convert(F f);
        }

        public ListAdapter(List<F> fromList2, Converter<F, T> converter2) {
            this.fromList = fromList2;
            this.converter = converter2;
        }

        @Override // java.util.AbstractList, java.util.List
        public T get(int index) {
            return this.converter.convert(this.fromList.get(index));
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
        public int size() {
            return this.fromList.size();
        }
    }

    public static class MapAdapter<K, V, RealValue> extends AbstractMap<K, V> {
        private final Map<K, RealValue> realMap;
        private final Converter<RealValue, V> valueConverter;

        public interface Converter<A, B> {
            A doBackward(B b);

            B doForward(A a);
        }

        public static <T extends EnumLite> Converter<Integer, T> newEnumConverter(final EnumLiteMap<T> enumMap, final T unrecognizedValue) {
            return new Converter<Integer, T>() {
                /* class com.android.framework.protobuf.Internal.MapAdapter.AnonymousClass1 */

                /* Return type fixed from 'java.lang.Object' to match base method */
                @Override // com.android.framework.protobuf.Internal.MapAdapter.Converter
                public /* bridge */ /* synthetic */ Integer doBackward(Object obj) {
                    return doBackward((AnonymousClass1) ((EnumLite) obj));
                }

                public T doForward(Integer value) {
                    T result = (T) EnumLiteMap.this.findValueByNumber(value.intValue());
                    return result == null ? (T) unrecognizedValue : result;
                }

                public Integer doBackward(T value) {
                    return Integer.valueOf(value.getNumber());
                }
            };
        }

        public MapAdapter(Map<K, RealValue> realMap2, Converter<RealValue, V> valueConverter2) {
            this.realMap = realMap2;
            this.valueConverter = valueConverter2;
        }

        @Override // java.util.AbstractMap, java.util.Map
        public V get(Object key) {
            RealValue result = this.realMap.get(key);
            if (result == null) {
                return null;
            }
            return this.valueConverter.doForward(result);
        }

        @Override // java.util.AbstractMap, java.util.Map
        public V put(K key, V value) {
            RealValue oldValue = this.realMap.put(key, this.valueConverter.doBackward(value));
            if (oldValue == null) {
                return null;
            }
            return this.valueConverter.doForward(oldValue);
        }

        @Override // java.util.AbstractMap, java.util.Map
        public Set<Map.Entry<K, V>> entrySet() {
            return new SetAdapter(this.realMap.entrySet());
        }

        private class SetAdapter extends AbstractSet<Map.Entry<K, V>> {
            private final Set<Map.Entry<K, RealValue>> realSet;

            public SetAdapter(Set<Map.Entry<K, RealValue>> realSet2) {
                this.realSet = realSet2;
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
            public Iterator<Map.Entry<K, V>> iterator() {
                return new IteratorAdapter(this.realSet.iterator());
            }

            @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
            public int size() {
                return this.realSet.size();
            }
        }

        private class IteratorAdapter implements Iterator<Map.Entry<K, V>> {
            private final Iterator<Map.Entry<K, RealValue>> realIterator;

            public IteratorAdapter(Iterator<Map.Entry<K, RealValue>> realIterator2) {
                this.realIterator = realIterator2;
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.realIterator.hasNext();
            }

            @Override // java.util.Iterator
            public Map.Entry<K, V> next() {
                return new EntryAdapter(this.realIterator.next());
            }

            @Override // java.util.Iterator
            public void remove() {
                this.realIterator.remove();
            }
        }

        /* access modifiers changed from: private */
        public class EntryAdapter implements Map.Entry<K, V> {
            private final Map.Entry<K, RealValue> realEntry;

            public EntryAdapter(Map.Entry<K, RealValue> realEntry2) {
                this.realEntry = realEntry2;
            }

            @Override // java.util.Map.Entry
            public K getKey() {
                return this.realEntry.getKey();
            }

            @Override // java.util.Map.Entry
            public V getValue() {
                return (V) MapAdapter.this.valueConverter.doForward(this.realEntry.getValue());
            }

            /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.Map$Entry<K, RealValue> */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Map.Entry
            public V setValue(V value) {
                RealValue oldValue = this.realEntry.setValue(MapAdapter.this.valueConverter.doBackward(value));
                if (oldValue == null) {
                    return null;
                }
                return (V) MapAdapter.this.valueConverter.doForward(oldValue);
            }
        }
    }
}
