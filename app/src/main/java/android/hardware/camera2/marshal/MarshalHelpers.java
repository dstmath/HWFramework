package android.hardware.camera2.marshal;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Rational;
import com.android.internal.util.Preconditions;

public final class MarshalHelpers {
    public static final int SIZEOF_BYTE = 1;
    public static final int SIZEOF_DOUBLE = 8;
    public static final int SIZEOF_FLOAT = 4;
    public static final int SIZEOF_INT32 = 4;
    public static final int SIZEOF_INT64 = 8;
    public static final int SIZEOF_RATIONAL = 8;

    public static int getPrimitiveTypeSize(int nativeType) {
        switch (nativeType) {
            case TextToSpeech.SUCCESS /*0*/:
                return SIZEOF_BYTE;
            case SIZEOF_BYTE /*1*/:
                return SIZEOF_INT32;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return SIZEOF_INT32;
            case Engine.DEFAULT_STREAM /*3*/:
                return SIZEOF_RATIONAL;
            case SIZEOF_INT32 /*4*/:
                return SIZEOF_RATIONAL;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return SIZEOF_RATIONAL;
            default:
                throw new UnsupportedOperationException("Unknown type, can't get size for " + nativeType);
        }
    }

    public static <T> Class<T> checkPrimitiveClass(Class<T> klass) {
        Preconditions.checkNotNull(klass, "klass must not be null");
        if (isPrimitiveClass(klass)) {
            return klass;
        }
        throw new UnsupportedOperationException("Unsupported class '" + klass + "'; expected a metadata primitive class");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T> boolean isPrimitiveClass(Class<T> klass) {
        if (klass == null) {
            return false;
        }
        return klass == Byte.TYPE || klass == Byte.class || klass == Integer.TYPE || klass == Integer.class || klass == Float.TYPE || klass == Float.class || klass == Long.TYPE || klass == Long.class || klass == Double.TYPE || klass == Double.class || klass == Rational.class;
    }

    public static <T> Class<T> wrapClassIfPrimitive(Class<T> klass) {
        if (klass == Byte.TYPE) {
            return Byte.class;
        }
        if (klass == Integer.TYPE) {
            return Integer.class;
        }
        if (klass == Float.TYPE) {
            return Float.class;
        }
        if (klass == Long.TYPE) {
            return Long.class;
        }
        if (klass == Double.TYPE) {
            return Double.class;
        }
        return klass;
    }

    public static String toStringNativeType(int nativeType) {
        switch (nativeType) {
            case TextToSpeech.SUCCESS /*0*/:
                return "TYPE_BYTE";
            case SIZEOF_BYTE /*1*/:
                return "TYPE_INT32";
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return "TYPE_FLOAT";
            case Engine.DEFAULT_STREAM /*3*/:
                return "TYPE_INT64";
            case SIZEOF_INT32 /*4*/:
                return "TYPE_DOUBLE";
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return "TYPE_RATIONAL";
            default:
                return "UNKNOWN(" + nativeType + ")";
        }
    }

    public static int checkNativeType(int nativeType) {
        switch (nativeType) {
            case TextToSpeech.SUCCESS /*0*/:
            case SIZEOF_BYTE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case SIZEOF_INT32 /*4*/:
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return nativeType;
            default:
                throw new UnsupportedOperationException("Unknown nativeType " + nativeType);
        }
    }

    public static int checkNativeTypeEquals(int expectedNativeType, int actualNativeType) {
        if (expectedNativeType == actualNativeType) {
            return actualNativeType;
        }
        throw new UnsupportedOperationException(String.format("Expected native type %d, but got %d", new Object[]{Integer.valueOf(expectedNativeType), Integer.valueOf(actualNativeType)}));
    }

    private MarshalHelpers() {
        throw new AssertionError();
    }
}
