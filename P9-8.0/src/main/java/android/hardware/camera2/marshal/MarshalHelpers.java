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
        switch (nativeType) {
            case 0:
                return 1;
            case 1:
                return 4;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 8;
            case 5:
                return 8;
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

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:12:0x0016, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:17:0x001f, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:22:0x0028, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:27:0x0031, code:
            return true;
     */
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
            case 0:
                return "TYPE_BYTE";
            case 1:
                return "TYPE_INT32";
            case 2:
                return "TYPE_FLOAT";
            case 3:
                return "TYPE_INT64";
            case 4:
                return "TYPE_DOUBLE";
            case 5:
                return "TYPE_RATIONAL";
            default:
                return "UNKNOWN(" + nativeType + ")";
        }
    }

    public static int checkNativeType(int nativeType) {
        switch (nativeType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
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
