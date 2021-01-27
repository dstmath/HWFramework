package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalHelpers;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Log;
import java.lang.Enum;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MarshalQueryableEnum<T extends Enum<T>> implements MarshalQueryable<T> {
    private static final boolean DEBUG = false;
    private static final String TAG = MarshalQueryableEnum.class.getSimpleName();
    private static final int UINT8_MASK = 255;
    private static final int UINT8_MAX = 255;
    private static final int UINT8_MIN = 0;
    private static final HashMap<Class<? extends Enum>, int[]> sEnumValues = new HashMap<>();

    private class MarshalerEnum extends Marshaler<T> {
        private final Class<T> mClass;

        /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: android.hardware.camera2.marshal.impl.MarshalQueryableEnum$MarshalerEnum */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.hardware.camera2.marshal.Marshaler
        public /* bridge */ /* synthetic */ void marshal(Object obj, ByteBuffer byteBuffer) {
            marshal((MarshalerEnum) ((Enum) obj), byteBuffer);
        }

        /* JADX DEBUG: Type inference failed for r1v1. Raw type applied. Possible types: java.lang.Class<? super T extends java.lang.Enum<T>>, java.lang.Class<T> */
        protected MarshalerEnum(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableEnum.this, typeReference, nativeType);
            this.mClass = (Class<? super T>) typeReference.getRawType();
        }

        public void marshal(T value, ByteBuffer buffer) {
            int enumValue = MarshalQueryableEnum.getEnumValue(value);
            if (this.mNativeType == 1) {
                buffer.putInt(enumValue);
            } else if (this.mNativeType != 0) {
                throw new AssertionError();
            } else if (enumValue < 0 || enumValue > 255) {
                throw new UnsupportedOperationException(String.format("Enum value %x too large to fit into unsigned byte", Integer.valueOf(enumValue)));
            } else {
                buffer.put((byte) enumValue);
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public T unmarshal(ByteBuffer buffer) {
            int enumValue;
            int i = this.mNativeType;
            if (i == 0) {
                enumValue = buffer.get() & 255;
            } else if (i == 1) {
                enumValue = buffer.getInt();
            } else {
                throw new AssertionError("Unexpected native type; impossible since its not supported");
            }
            return (T) MarshalQueryableEnum.getEnumFromValue(this.mClass, enumValue);
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return MarshalHelpers.getPrimitiveTypeSize(this.mNativeType);
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerEnum(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        if ((nativeType == 1 || nativeType == 0) && (managedType.getType() instanceof Class)) {
            Class<?> typeClass = (Class) managedType.getType();
            if (typeClass.isEnum()) {
                try {
                    typeClass.getDeclaredConstructor(String.class, Integer.TYPE);
                    return true;
                } catch (NoSuchMethodException e) {
                    String str = TAG;
                    Log.e(str, "Can't marshal class " + typeClass + "; no default constructor");
                } catch (SecurityException e2) {
                    String str2 = TAG;
                    Log.e(str2, "Can't marshal class " + typeClass + "; not accessible");
                }
            }
        }
        return false;
    }

    public static <T extends Enum<T>> void registerEnumValues(Class<T> enumType, int[] values) {
        if (enumType.getEnumConstants().length == values.length) {
            sEnumValues.put(enumType, values);
            return;
        }
        throw new IllegalArgumentException("Expected values array to be the same size as the enumTypes values " + values.length + " for type " + enumType);
    }

    /* access modifiers changed from: private */
    public static <T extends Enum<T>> int getEnumValue(T enumValue) {
        int[] values = sEnumValues.get(enumValue.getClass());
        int ordinal = enumValue.ordinal();
        if (values != null) {
            return values[ordinal];
        }
        return ordinal;
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0041: APUT  
      (r4v1 java.lang.Object[])
      (2 ??[int, float, short, byte, char])
      (wrap: java.lang.Boolean : 0x003d: INVOKE  (r5v3 java.lang.Boolean) = (r5v2 boolean) type: STATIC call: java.lang.Boolean.valueOf(boolean):java.lang.Boolean)
     */
    public static <T extends Enum<T>> T getEnumFromValue(Class<T> enumType, int value) {
        int ordinal;
        int[] registeredValues = sEnumValues.get(enumType);
        if (registeredValues != null) {
            ordinal = -1;
            int i = 0;
            while (true) {
                if (i >= registeredValues.length) {
                    break;
                } else if (registeredValues[i] == value) {
                    ordinal = i;
                    break;
                } else {
                    i++;
                }
            }
        } else {
            ordinal = value;
        }
        T[] values = enumType.getEnumConstants();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(value);
        boolean z = true;
        objArr[1] = enumType;
        if (registeredValues == null) {
            z = false;
        }
        objArr[2] = Boolean.valueOf(z);
        throw new IllegalArgumentException(String.format("Argument 'value' (%d) was not a valid enum value for type %s (registered? %b)", objArr));
    }
}
