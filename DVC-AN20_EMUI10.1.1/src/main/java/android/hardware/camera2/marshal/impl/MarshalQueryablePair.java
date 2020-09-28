package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.MarshalRegistry;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;

public class MarshalQueryablePair<T1, T2> implements MarshalQueryable<Pair<T1, T2>> {

    private class MarshalerPair extends Marshaler<Pair<T1, T2>> {
        private final Class<? super Pair<T1, T2>> mClass;
        private final Constructor<Pair<T1, T2>> mConstructor;
        private final Marshaler<T1> mNestedTypeMarshalerFirst;
        private final Marshaler<T2> mNestedTypeMarshalerSecond;

        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ int calculateMarshalSize(Object obj) {
            return calculateMarshalSize((Pair) ((Pair) obj));
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ void marshal(Object obj, ByteBuffer byteBuffer) {
            marshal((Pair) ((Pair) obj), byteBuffer);
        }

        /* JADX DEBUG: Type inference failed for r0v7. Raw type applied. Possible types: java.lang.reflect.Constructor<? super android.util.Pair<T1, T2>>, java.lang.reflect.Constructor<android.util.Pair<T1, T2>> */
        protected MarshalerPair(TypeReference<Pair<T1, T2>> typeReference, int nativeType) {
            super(MarshalQueryablePair.this, typeReference, nativeType);
            this.mClass = typeReference.getRawType();
            try {
                ParameterizedType paramType = (ParameterizedType) typeReference.getType();
                this.mNestedTypeMarshalerFirst = MarshalRegistry.getMarshaler(TypeReference.createSpecializedTypeReference(paramType.getActualTypeArguments()[0]), this.mNativeType);
                this.mNestedTypeMarshalerSecond = MarshalRegistry.getMarshaler(TypeReference.createSpecializedTypeReference(paramType.getActualTypeArguments()[1]), this.mNativeType);
                try {
                    this.mConstructor = (Constructor<? super Pair<T1, T2>>) this.mClass.getConstructor(Object.class, Object.class);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            } catch (ClassCastException e2) {
                throw new AssertionError("Raw use of Pair is not supported", e2);
            }
        }

        public void marshal(Pair<T1, T2> value, ByteBuffer buffer) {
            if (value.first == null) {
                throw new UnsupportedOperationException("Pair#first must not be null");
            } else if (value.second != null) {
                this.mNestedTypeMarshalerFirst.marshal(value.first, buffer);
                this.mNestedTypeMarshalerSecond.marshal(value.second, buffer);
            } else {
                throw new UnsupportedOperationException("Pair#second must not be null");
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public Pair<T1, T2> unmarshal(ByteBuffer buffer) {
            T1 first = this.mNestedTypeMarshalerFirst.unmarshal(buffer);
            T2 second = this.mNestedTypeMarshalerSecond.unmarshal(buffer);
            try {
                return this.mConstructor.newInstance(first, second);
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
            int firstSize = this.mNestedTypeMarshalerFirst.getNativeSize();
            int secondSize = this.mNestedTypeMarshalerSecond.getNativeSize();
            if (firstSize == NATIVE_SIZE_DYNAMIC || secondSize == NATIVE_SIZE_DYNAMIC) {
                return NATIVE_SIZE_DYNAMIC;
            }
            return firstSize + secondSize;
        }

        public int calculateMarshalSize(Pair<T1, T2> value) {
            int nativeSize = getNativeSize();
            if (nativeSize != NATIVE_SIZE_DYNAMIC) {
                return nativeSize;
            }
            return this.mNestedTypeMarshalerFirst.calculateMarshalSize(value.first) + this.mNestedTypeMarshalerSecond.calculateMarshalSize(value.second);
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<Pair<T1, T2>> createMarshaler(TypeReference<Pair<T1, T2>> managedType, int nativeType) {
        return new MarshalerPair(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<Pair<T1, T2>> managedType, int nativeType) {
        return Pair.class.equals(managedType.getRawType());
    }
}
