package sun.invoke.util;

import java.lang.reflect.Array;
import java.sql.Types;
import java.util.Arrays;

public enum Wrapper {
    BOOLEAN(Boolean.class, Boolean.TYPE, 'Z', Boolean.valueOf(false), new boolean[0], Format.unsigned(1)),
    BYTE(Byte.class, Byte.TYPE, 'B', Byte.valueOf((byte) 0), new byte[0], Format.signed(8)),
    SHORT(Short.class, Short.TYPE, 'S', Short.valueOf((short) 0), new short[0], Format.signed(16)),
    CHAR(Character.class, Character.TYPE, 'C', Character.valueOf(0), new char[0], Format.unsigned(16)),
    INT(Integer.class, Integer.TYPE, 'I', Integer.valueOf(0), new int[0], Format.signed(32)),
    LONG(Long.class, Long.TYPE, 'J', Long.valueOf(0), new long[0], Format.signed(64)),
    FLOAT(Float.class, Float.TYPE, 'F', Float.valueOf(0.0f), new float[0], Format.floating(32)),
    DOUBLE(Double.class, Double.TYPE, 'D', Double.valueOf(0.0d), new double[0], Format.floating(64)),
    OBJECT(Object.class, Object.class, 'L', null, new Object[0], Format.other(1)),
    VOID(Void.class, Void.TYPE, 'V', null, null, Format.other(0));
    
    private static final Wrapper[] FROM_CHAR = null;
    private static final Wrapper[] FROM_PRIM = null;
    private static final Wrapper[] FROM_WRAP = null;
    private final char basicTypeChar;
    private final Object emptyArray;
    private final int format;
    private final String primitiveSimpleName;
    private final Class<?> primitiveType;
    private final String wrapperSimpleName;
    private final Class<?> wrapperType;
    private final Object zero;

    private static abstract class Format {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        static final int BOOLEAN = 5;
        static final int CHAR = 65;
        static final int FLOAT = 4225;
        static final int FLOATING = 4096;
        static final int INT = -3967;
        static final int KIND_SHIFT = 12;
        static final int NUM_MASK = -4;
        static final int SHORT = -4031;
        static final int SIGNED = -4096;
        static final int SIZE_MASK = 1023;
        static final int SIZE_SHIFT = 2;
        static final int SLOT_MASK = 3;
        static final int SLOT_SHIFT = 0;
        static final int UNSIGNED = 0;
        static final int VOID = 0;

        static {
            -assertionsDisabled = Format.class.desiredAssertionStatus() ^ 1;
        }

        private Format() {
        }

        static int format(int kind, int size, int slots) {
            Object obj = 1;
            if (!-assertionsDisabled && ((kind >> 12) << 12) != kind) {
                throw new AssertionError();
            } else if (-assertionsDisabled || ((size - 1) & size) == 0) {
                if (!-assertionsDisabled) {
                    Object obj2;
                    int obj22;
                    if (kind == SIGNED) {
                        obj22 = size > 0 ? 1 : null;
                    } else if (kind == 0) {
                        if (size > 0) {
                            obj22 = 1;
                        } else {
                            obj22 = null;
                        }
                    } else if (kind != 4096) {
                        obj22 = null;
                    } else if (size == 32 || size == 64) {
                        obj22 = 1;
                    } else {
                        obj22 = null;
                    }
                    if (obj22 == null) {
                        throw new AssertionError();
                    }
                }
                if (!-assertionsDisabled) {
                    if (slots == 2) {
                        if (size != 64) {
                            obj = null;
                        }
                    } else if (slots != 1) {
                        obj = null;
                    } else if (size > 32) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                return ((size << 2) | kind) | (slots << 0);
            } else {
                throw new AssertionError();
            }
        }

        static int signed(int size) {
            return format(SIGNED, size, size > 32 ? 2 : 1);
        }

        static int unsigned(int size) {
            return format(0, size, size > 32 ? 2 : 1);
        }

        static int floating(int size) {
            return format(4096, size, size > 32 ? 2 : 1);
        }

        static int other(int slots) {
            return slots << 0;
        }
    }

    private Wrapper(Class<?> wtype, Class<?> ptype, char tchar, Object zero, Object emptyArray, int format) {
        this.wrapperType = wtype;
        this.primitiveType = ptype;
        this.basicTypeChar = tchar;
        this.zero = zero;
        this.emptyArray = emptyArray;
        this.format = format;
        this.wrapperSimpleName = wtype.getSimpleName();
        this.primitiveSimpleName = ptype.getSimpleName();
    }

    public String detailString() {
        return this.wrapperSimpleName + Arrays.asList(this.wrapperType, this.primitiveType, Character.valueOf(this.basicTypeChar), this.zero, "0x" + Integer.toHexString(this.format));
    }

    public int bitWidth() {
        return (this.format >> 2) & 1023;
    }

    public int stackSlots() {
        return (this.format >> 0) & 3;
    }

    public boolean isSingleWord() {
        return (this.format & 1) != 0;
    }

    public boolean isDoubleWord() {
        return (this.format & 2) != 0;
    }

    public boolean isNumeric() {
        return (this.format & -4) != 0;
    }

    public boolean isIntegral() {
        return isNumeric() && this.format < 4225;
    }

    public boolean isSubwordOrInt() {
        return isIntegral() ? isSingleWord() : false;
    }

    public boolean isSigned() {
        return this.format < 0;
    }

    public boolean isUnsigned() {
        return this.format >= 5 && this.format < 4225;
    }

    public boolean isFloating() {
        return this.format >= 4225;
    }

    public boolean isOther() {
        return (this.format & -4) == 0;
    }

    public boolean isConvertibleFrom(Wrapper source) {
        if (this == source) {
            return true;
        }
        if (compareTo((Enum) source) < 0) {
            return false;
        }
        if (!(((this.format & source.format) & -4096) != 0)) {
            return isOther() || source.format == 65;
        } else {
            if (!-assertionsDisabled) {
                if (!(!isFloating() ? isSigned() : true)) {
                    throw new AssertionError();
                }
            }
            if (!-assertionsDisabled) {
                if (!(!source.isFloating() ? source.isSigned() : true)) {
                    throw new AssertionError();
                }
            }
            return true;
        }
    }

    private static boolean checkConvertibleFrom() {
        Wrapper[] values = values();
        int length = values.length;
        int i = 0;
        while (i < length) {
            Wrapper w = values[i];
            if (!-assertionsDisabled && !w.isConvertibleFrom(w)) {
                throw new AssertionError();
            } else if (-assertionsDisabled || VOID.isConvertibleFrom(w)) {
                if (w != VOID) {
                    if (!-assertionsDisabled && !OBJECT.isConvertibleFrom(w)) {
                        throw new AssertionError();
                    } else if (!-assertionsDisabled && w.isConvertibleFrom(VOID)) {
                        throw new AssertionError();
                    }
                }
                if (w != CHAR) {
                    if (!-assertionsDisabled && CHAR.isConvertibleFrom(w)) {
                        throw new AssertionError();
                    } else if (!(w.isConvertibleFrom(INT) || -assertionsDisabled || !w.isConvertibleFrom(CHAR))) {
                        throw new AssertionError();
                    }
                }
                if (w != BOOLEAN) {
                    if (!-assertionsDisabled && BOOLEAN.isConvertibleFrom(w)) {
                        throw new AssertionError();
                    } else if (!(w == VOID || w == OBJECT || -assertionsDisabled || !w.isConvertibleFrom(BOOLEAN))) {
                        throw new AssertionError();
                    }
                }
                if (w.isSigned()) {
                    for (Wrapper x : values()) {
                        if (w != x) {
                            if (x.isFloating()) {
                                if (!-assertionsDisabled && w.isConvertibleFrom(x)) {
                                    throw new AssertionError();
                                }
                            } else if (!x.isSigned()) {
                                continue;
                            } else if (w.compareTo((Enum) x) < 0) {
                                if (!-assertionsDisabled && w.isConvertibleFrom(x)) {
                                    throw new AssertionError();
                                }
                            } else if (!(-assertionsDisabled || w.isConvertibleFrom(x))) {
                                throw new AssertionError();
                            }
                        }
                    }
                }
                if (w.isFloating()) {
                    for (Wrapper x2 : values()) {
                        if (w != x2) {
                            if (x2.isSigned()) {
                                if (!(-assertionsDisabled || w.isConvertibleFrom(x2))) {
                                    throw new AssertionError();
                                }
                            } else if (!x2.isFloating()) {
                                continue;
                            } else if (w.compareTo((Enum) x2) < 0) {
                                if (!-assertionsDisabled && w.isConvertibleFrom(x2)) {
                                    throw new AssertionError();
                                }
                            } else if (!(-assertionsDisabled || w.isConvertibleFrom(x2))) {
                                throw new AssertionError();
                            }
                        }
                    }
                    continue;
                }
                i++;
            } else {
                throw new AssertionError();
            }
        }
        return true;
    }

    public Object zero() {
        return this.zero;
    }

    public <T> T zero(Class<T> type) {
        return convert(this.zero, type);
    }

    public static Wrapper forPrimitiveType(Class<?> type) {
        Wrapper w = findPrimitiveType(type);
        if (w != null) {
            return w;
        }
        if (type.isPrimitive()) {
            throw new InternalError();
        }
        throw newIllegalArgumentException("not primitive: " + type);
    }

    static Wrapper findPrimitiveType(Class<?> type) {
        Wrapper w = FROM_PRIM[hashPrim(type)];
        if (w == null || w.primitiveType != type) {
            return null;
        }
        return w;
    }

    public static Wrapper forWrapperType(Class<?> type) {
        Wrapper w = findWrapperType(type);
        if (w != null) {
            return w;
        }
        for (Wrapper x : values()) {
            if (x.wrapperType == type) {
                throw new InternalError();
            }
        }
        throw newIllegalArgumentException("not wrapper: " + type);
    }

    static Wrapper findWrapperType(Class<?> type) {
        Wrapper w = FROM_WRAP[hashWrap(type)];
        if (w == null || w.wrapperType != type) {
            return null;
        }
        return w;
    }

    public static Wrapper forBasicType(char type) {
        Wrapper w = FROM_CHAR[hashChar(type)];
        if (w != null && w.basicTypeChar == type) {
            return w;
        }
        for (Wrapper x : values()) {
            if (w.basicTypeChar == type) {
                throw new InternalError();
            }
        }
        throw newIllegalArgumentException("not basic type char: " + type);
    }

    public static Wrapper forBasicType(Class<?> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type);
        }
        return OBJECT;
    }

    private static int hashPrim(Class<?> x) {
        String xn = x.getName();
        if (xn.length() < 3) {
            return 0;
        }
        return (xn.charAt(0) + xn.charAt(2)) % 16;
    }

    private static int hashWrap(Class<?> x) {
        String xn = x.getName();
        if (!-assertionsDisabled && 10 != "java.lang.".length()) {
            throw new AssertionError();
        } else if (xn.length() < 13) {
            return 0;
        } else {
            return ((xn.charAt(11) * 3) + xn.charAt(12)) % 16;
        }
    }

    private static int hashChar(char x) {
        return ((x >> 1) + x) % 16;
    }

    public Class<?> primitiveType() {
        return this.primitiveType;
    }

    public Class<?> wrapperType() {
        return this.wrapperType;
    }

    public <T> Class<T> wrapperType(Class<T> exampleType) {
        if (exampleType == this.wrapperType) {
            return exampleType;
        }
        if (exampleType == this.primitiveType || this.wrapperType == Object.class || exampleType.isInterface()) {
            return forceType(this.wrapperType, exampleType);
        }
        throw newClassCastException(exampleType, this.primitiveType);
    }

    private static ClassCastException newClassCastException(Class<?> actual, Class<?> expected) {
        return new ClassCastException(actual + " is not compatible with " + expected);
    }

    public static <T> Class<T> asWrapperType(Class<T> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type).wrapperType(type);
        }
        return type;
    }

    public static <T> Class<T> asPrimitiveType(Class<T> type) {
        Wrapper w = findWrapperType(type);
        if (w != null) {
            return forceType(w.primitiveType(), type);
        }
        return type;
    }

    public static boolean isWrapperType(Class<?> type) {
        return findWrapperType(type) != null;
    }

    public static boolean isPrimitiveType(Class<?> type) {
        return type.isPrimitive();
    }

    public static char basicTypeChar(Class<?> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type).basicTypeChar();
        }
        return 'L';
    }

    public char basicTypeChar() {
        return this.basicTypeChar;
    }

    public String wrapperSimpleName() {
        return this.wrapperSimpleName;
    }

    public String primitiveSimpleName() {
        return this.primitiveSimpleName;
    }

    public <T> T cast(Object x, Class<T> type) {
        return convert(x, type, true);
    }

    public <T> T convert(Object x, Class<T> type) {
        return convert(x, type, false);
    }

    private <T> T convert(Object x, Class<T> type, boolean isCast) {
        T result;
        if (this != OBJECT) {
            Class<T> wtype = wrapperType(type);
            if (wtype.isInstance(x)) {
                return wtype.cast(x);
            }
            if (!isCast) {
                Class<?> sourceType = x.getClass();
                Wrapper source = findWrapperType(sourceType);
                if (source == null || (isConvertibleFrom(source) ^ 1) != 0) {
                    throw newClassCastException(wtype, sourceType);
                }
            } else if (x == null) {
                return this.zero;
            }
            result = wrap(x);
            if (!-assertionsDisabled) {
                if ((result == null ? Void.class : result.getClass()) != wtype) {
                    throw new AssertionError();
                }
            }
            return result;
        } else if (-assertionsDisabled || !type.isPrimitive()) {
            if (!type.isInterface()) {
                type.cast(x);
            }
            result = x;
            return x;
        } else {
            throw new AssertionError();
        }
    }

    static <T> Class<T> forceType(Class<?> type, Class<T> exampleType) {
        boolean z = (type == exampleType || ((type.isPrimitive() && forPrimitiveType(type) == findWrapperType(exampleType)) || (exampleType.isPrimitive() && forPrimitiveType(exampleType) == findWrapperType(type)))) ? true : type == Object.class ? exampleType.isPrimitive() ^ 1 : false;
        if (!z) {
            System.out.println(type + " <= " + exampleType);
        }
        if (!-assertionsDisabled) {
            int isPrimitive = (type == exampleType || ((type.isPrimitive() && forPrimitiveType(type) == findWrapperType(exampleType)) || (exampleType.isPrimitive() && forPrimitiveType(exampleType) == findWrapperType(type)))) ? 1 : type == Object.class ? exampleType.isPrimitive() ^ 1 : 0;
            if (isPrimitive == 0) {
                throw new AssertionError();
            }
        }
        Class<T> result = type;
        return type;
    }

    public Object wrap(Object x) {
        switch (this.basicTypeChar) {
            case 'L':
                return x;
            case 'V':
                return null;
            default:
                Number xn = numberValue(x);
                switch (this.basicTypeChar) {
                    case 'B':
                        return Byte.valueOf((byte) xn.intValue());
                    case 'C':
                        return Character.valueOf((char) xn.intValue());
                    case 'D':
                        return Double.valueOf(xn.doubleValue());
                    case Types.DATALINK /*70*/:
                        return Float.valueOf(xn.floatValue());
                    case 'I':
                        return Integer.valueOf(xn.intValue());
                    case 'J':
                        return Long.valueOf(xn.longValue());
                    case 'S':
                        return Short.valueOf((short) xn.intValue());
                    case 'Z':
                        return Boolean.valueOf(boolValue(xn.byteValue()));
                    default:
                        throw new InternalError("bad wrapper");
                }
        }
    }

    public Object wrap(int x) {
        if (this.basicTypeChar == 'L') {
            return Integer.valueOf(x);
        }
        switch (this.basicTypeChar) {
            case 'B':
                return Byte.valueOf((byte) x);
            case 'C':
                return Character.valueOf((char) x);
            case 'D':
                return Double.valueOf((double) x);
            case Types.DATALINK /*70*/:
                return Float.valueOf((float) x);
            case 'I':
                return Integer.valueOf(x);
            case 'J':
                return Long.valueOf((long) x);
            case 'L':
                throw newIllegalArgumentException("cannot wrap to object type");
            case 'S':
                return Short.valueOf((short) x);
            case 'V':
                return null;
            case 'Z':
                return Boolean.valueOf(boolValue((byte) x));
            default:
                throw new InternalError("bad wrapper");
        }
    }

    private static Number numberValue(Object x) {
        if (x instanceof Number) {
            return (Number) x;
        }
        if (x instanceof Character) {
            return Integer.valueOf(((Character) x).charValue());
        }
        if (!(x instanceof Boolean)) {
            return (Number) x;
        }
        return Integer.valueOf(((Boolean) x).booleanValue() ? 1 : 0);
    }

    private static boolean boolValue(byte bits) {
        if (((byte) (bits & 1)) != (byte) 0) {
            return true;
        }
        return false;
    }

    private static RuntimeException newIllegalArgumentException(String message, Object x) {
        return newIllegalArgumentException(message + x);
    }

    private static RuntimeException newIllegalArgumentException(String message) {
        return new IllegalArgumentException(message);
    }

    public Object makeArray(int len) {
        return Array.newInstance(this.primitiveType, len);
    }

    public Class<?> arrayType() {
        return this.emptyArray.getClass();
    }

    public void copyArrayUnboxing(Object[] values, int vpos, Object a, int apos, int length) {
        if (a.getClass() != arrayType()) {
            arrayType().cast(a);
        }
        for (int i = 0; i < length; i++) {
            Array.set(a, i + apos, convert(values[i + vpos], this.primitiveType));
        }
    }

    public void copyArrayBoxing(Object a, int apos, Object[] values, int vpos, int length) {
        if (a.getClass() != arrayType()) {
            arrayType().cast(a);
        }
        int i = 0;
        while (i < length) {
            Object value = Array.get(a, i + apos);
            if (-assertionsDisabled || value.getClass() == this.wrapperType) {
                values[i + vpos] = value;
                i++;
            } else {
                throw new AssertionError();
            }
        }
    }
}
