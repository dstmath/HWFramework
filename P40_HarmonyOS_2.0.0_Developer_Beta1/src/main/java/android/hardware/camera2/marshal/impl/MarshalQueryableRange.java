package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.MarshalRegistry;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Range;
import java.lang.Comparable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;

public class MarshalQueryableRange<T extends Comparable<? super T>> implements MarshalQueryable<Range<T>> {
    private static final int RANGE_COUNT = 2;

    private class MarshalerRange extends Marshaler<Range<T>> {
        private final Class<? super Range<T>> mClass;
        private final Constructor<Range<T>> mConstructor;
        private final Marshaler<T> mNestedTypeMarshaler;

        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ int calculateMarshalSize(Object obj) {
            return calculateMarshalSize((Range) ((Range) obj));
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ void marshal(Object obj, ByteBuffer byteBuffer) {
            marshal((Range) ((Range) obj), byteBuffer);
        }

        /* JADX DEBUG: Type inference failed for r1v4. Raw type applied. Possible types: java.lang.reflect.Constructor<? super android.util.Range<T>>, java.lang.reflect.Constructor<android.util.Range<T>> */
        protected MarshalerRange(TypeReference<Range<T>> typeReference, int nativeType) {
            super(MarshalQueryableRange.this, typeReference, nativeType);
            this.mClass = typeReference.getRawType();
            try {
                this.mNestedTypeMarshaler = MarshalRegistry.getMarshaler(TypeReference.createSpecializedTypeReference(((ParameterizedType) typeReference.getType()).getActualTypeArguments()[0]), this.mNativeType);
                try {
                    this.mConstructor = (Constructor<? super Range<T>>) this.mClass.getConstructor(Comparable.class, Comparable.class);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            } catch (ClassCastException e2) {
                throw new AssertionError("Raw use of Range is not supported", e2);
            }
        }

        public void marshal(Range<T> value, ByteBuffer buffer) {
            this.mNestedTypeMarshaler.marshal(value.getLower(), buffer);
            this.mNestedTypeMarshaler.marshal(value.getUpper(), buffer);
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public Range<T> unmarshal(ByteBuffer buffer) {
            try {
                return (Range) ((Range<T>) this.mConstructor.newInstance(this.mNestedTypeMarshaler.unmarshal(buffer), this.mNestedTypeMarshaler.unmarshal(buffer)));
            } catch (InstantiationException e) {
                throw new AssertionError(e);
            } catch (IllegalAccessException e2) {
                throw new AssertionError(e2);
            } catch (IllegalArgumentException e3) {
                throw new AssertionError(e3);
            } catch (InvocationTargetException e4) {
                throw new AssertionError(e4);
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            int nestedSize = this.mNestedTypeMarshaler.getNativeSize();
            if (nestedSize != NATIVE_SIZE_DYNAMIC) {
                return nestedSize * 2;
            }
            return NATIVE_SIZE_DYNAMIC;
        }

        public int calculateMarshalSize(Range<T> value) {
            int nativeSize = getNativeSize();
            if (nativeSize != NATIVE_SIZE_DYNAMIC) {
                return nativeSize;
            }
            return this.mNestedTypeMarshaler.calculateMarshalSize(value.getLower()) + this.mNestedTypeMarshaler.calculateMarshalSize(value.getUpper());
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<Range<T>> createMarshaler(TypeReference<Range<T>> managedType, int nativeType) {
        return new MarshalerRange(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<Range<T>> managedType, int nativeType) {
        return Range.class.equals(managedType.getRawType());
    }
}
