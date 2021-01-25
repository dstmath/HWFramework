package android.hardware.camera2.marshal;

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
        if (nativeType == 0) {
            return 1;
        }
        if (nativeType == 1 || nativeType == 2) {
            return 4;
        }
        if (nativeType == 3 || nativeType == 4 || nativeType == 5) {
            return 8;
        }
        throw new UnsupportedOperationException("Unknown type, can't get size for " + nativeType);
    }

    public static <T> Class<T> checkPrimitiveClass(Class<T> klass) {
        Preconditions.checkNotNull(klass, "klass must not be null");
        if (isPrimitiveClass(klass)) {
            return klass;
        }
        throw new UnsupportedOperationException("Unsupported class '" + klass + "'; expected a metadata primitive class");
    }

    public static <T> boolean isPrimitiveClass(Class<T> klass) {
        if (klass == null) {
            return false;
        }
        if (klass == Byte.TYPE || klass == Byte.class || klass == Integer.TYPE || klass == Integer.class || klass == Float.TYPE || klass == Float.class || klass == Long.TYPE || klass == Long.class || klass == Double.TYPE || klass == Double.class || klass == Rational.class) {
            return true;
        }
        return false;
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
        if (nativeType == 0) {
            return "TYPE_BYTE";
        }
        if (nativeType == 1) {
            return "TYPE_INT32";
        }
        if (nativeType == 2) {
            return "TYPE_FLOAT";
        }
        if (nativeType == 3) {
            return "TYPE_INT64";
        }
        if (nativeType == 4) {
            return "TYPE_DOUBLE";
        }
        if (nativeType == 5) {
            return "TYPE_RATIONAL";
        }
        return "UNKNOWN(" + nativeType + ")";
    }

    public static int checkNativeType(int nativeType) {
        if (nativeType == 0 || nativeType == 1 || nativeType == 2 || nativeType == 3 || nativeType == 4 || nativeType == 5) {
            return nativeType;
        }
        throw new UnsupportedOperationException("Unknown nativeType " + nativeType);
    }

    public static int checkNativeTypeEquals(int expectedNativeType, int actualNativeType) {
        if (expectedNativeType == actualNativeType) {
            return actualNativeType;
        }
        throw new UnsupportedOperationException(String.format("Expected native type %d, but got %d", Integer.valueOf(expectedNativeType), Integer.valueOf(actualNativeType)));
    }

    private MarshalHelpers() {
        throw new AssertionError();
    }
}
