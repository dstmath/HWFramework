package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalHelpers;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.speech.tts.TextToSpeech;
import android.telecom.AudioState;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MarshalQueryableEnum<T extends Enum<T>> implements MarshalQueryable<T> {
    private static final boolean DEBUG = false;
    private static final String TAG = null;
    private static final int UINT8_MASK = 255;
    private static final int UINT8_MAX = 255;
    private static final int UINT8_MIN = 0;
    private static final HashMap<Class<? extends Enum>, int[]> sEnumValues = null;

    private class MarshalerEnum extends Marshaler<T> {
        private final Class<T> mClass;

        protected MarshalerEnum(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableEnum.this, typeReference, nativeType);
            this.mClass = typeReference.getRawType();
        }

        public void marshal(T value, ByteBuffer buffer) {
            int enumValue = MarshalQueryableEnum.getEnumValue(value);
            if (this.mNativeType == 1) {
                buffer.putInt(enumValue);
            } else if (this.mNativeType != 0) {
                throw new AssertionError();
            } else if (enumValue < 0 || enumValue > MarshalQueryableEnum.UINT8_MAX) {
                throw new UnsupportedOperationException(String.format("Enum value %x too large to fit into unsigned byte", new Object[]{Integer.valueOf(enumValue)}));
            } else {
                buffer.put((byte) enumValue);
            }
        }

        public T unmarshal(ByteBuffer buffer) {
            int enumValue;
            switch (this.mNativeType) {
                case TextToSpeech.SUCCESS /*0*/:
                    enumValue = buffer.get() & MarshalQueryableEnum.UINT8_MAX;
                    break;
                case AudioState.ROUTE_EARPIECE /*1*/:
                    enumValue = buffer.getInt();
                    break;
                default:
                    throw new AssertionError("Unexpected native type; impossible since its not supported");
            }
            return MarshalQueryableEnum.getEnumFromValue(this.mClass, enumValue);
        }

        public int getNativeSize() {
            return MarshalHelpers.getPrimitiveTypeSize(this.mNativeType);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.marshal.impl.MarshalQueryableEnum.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.marshal.impl.MarshalQueryableEnum.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.marshal.impl.MarshalQueryableEnum.<clinit>():void");
    }

    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerEnum(managedType, nativeType);
    }

    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        if ((nativeType == 1 || nativeType == 0) && (managedType.getType() instanceof Class)) {
            Class<?> typeClass = (Class) managedType.getType();
            if (typeClass.isEnum()) {
                try {
                    typeClass.getDeclaredConstructor(new Class[]{String.class, Integer.TYPE});
                    return true;
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "Can't marshal class " + typeClass + "; no default constructor");
                } catch (SecurityException e2) {
                    Log.e(TAG, "Can't marshal class " + typeClass + "; not accessible");
                }
            }
        }
        return DEBUG;
    }

    public static <T extends Enum<T>> void registerEnumValues(Class<T> enumType, int[] values) {
        if (((Enum[]) enumType.getEnumConstants()).length != values.length) {
            throw new IllegalArgumentException("Expected values array to be the same size as the enumTypes values " + values.length + " for type " + enumType);
        }
        sEnumValues.put(enumType, values);
    }

    private static <T extends Enum<T>> int getEnumValue(T enumValue) {
        int[] values = (int[]) sEnumValues.get(enumValue.getClass());
        int ordinal = enumValue.ordinal();
        if (values != null) {
            return values[ordinal];
        }
        return ordinal;
    }

    private static <T extends Enum<T>> T getEnumFromValue(Class<T> enumType, int value) {
        int ordinal;
        boolean z = true;
        int[] registeredValues = (int[]) sEnumValues.get(enumType);
        if (registeredValues != null) {
            ordinal = -1;
            for (int i = 0; i < registeredValues.length; i++) {
                if (registeredValues[i] == value) {
                    ordinal = i;
                    break;
                }
            }
        } else {
            ordinal = value;
        }
        Enum[] values = (Enum[]) enumType.getEnumConstants();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        String str = "Argument 'value' (%d) was not a valid enum value for type %s (registered? %b)";
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(value);
        objArr[1] = enumType;
        if (registeredValues == null) {
            z = DEBUG;
        }
        objArr[2] = Boolean.valueOf(z);
        throw new IllegalArgumentException(String.format(str, objArr));
    }
}
