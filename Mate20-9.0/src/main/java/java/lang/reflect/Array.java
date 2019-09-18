package java.lang.reflect;

public final class Array {
    private static native Object createMultiArray(Class<?> cls, int[] iArr) throws NegativeArraySizeException;

    private static native Object createObjectArray(Class<?> cls, int i) throws NegativeArraySizeException;

    private Array() {
    }

    public static Object newInstance(Class<?> componentType, int length) throws NegativeArraySizeException {
        return newArray(componentType, length);
    }

    public static Object newInstance(Class<?> componentType, int... dimensions) throws IllegalArgumentException, NegativeArraySizeException {
        if (dimensions.length <= 0 || dimensions.length > 255) {
            throw new IllegalArgumentException("Bad number of dimensions: " + dimensions.length);
        } else if (componentType == Void.TYPE) {
            throw new IllegalArgumentException("Can't allocate an array of void");
        } else if (componentType != null) {
            return createMultiArray(componentType, dimensions);
        } else {
            throw new NullPointerException("componentType == null");
        }
    }

    public static int getLength(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).length;
        }
        if (array instanceof boolean[]) {
            return ((boolean[]) array).length;
        }
        if (array instanceof byte[]) {
            return ((byte[]) array).length;
        }
        if (array instanceof char[]) {
            return ((char[]) array).length;
        }
        if (array instanceof double[]) {
            return ((double[]) array).length;
        }
        if (array instanceof float[]) {
            return ((float[]) array).length;
        }
        if (array instanceof int[]) {
            return ((int[]) array).length;
        }
        if (array instanceof long[]) {
            return ((long[]) array).length;
        }
        if (array instanceof short[]) {
            return ((short[]) array).length;
        }
        throw badArray(array);
    }

    public static Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof Object[]) {
            return ((Object[]) array)[index];
        }
        if (array instanceof boolean[]) {
            return ((boolean[]) array)[index] ? Boolean.TRUE : Boolean.FALSE;
        } else if (array instanceof byte[]) {
            return Byte.valueOf(((byte[]) array)[index]);
        } else {
            if (array instanceof char[]) {
                return Character.valueOf(((char[]) array)[index]);
            }
            if (array instanceof short[]) {
                return Short.valueOf(((short[]) array)[index]);
            }
            if (array instanceof int[]) {
                return Integer.valueOf(((int[]) array)[index]);
            }
            if (array instanceof long[]) {
                return Long.valueOf(((long[]) array)[index]);
            }
            if (array instanceof float[]) {
                return new Float(((float[]) array)[index]);
            }
            if (array instanceof double[]) {
                return new Double(((double[]) array)[index]);
            }
            if (array == null) {
                throw new NullPointerException("array == null");
            }
            throw notAnArray(array);
        }
    }

    public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof boolean[]) {
            return ((boolean[]) array)[index];
        }
        throw badArray(array);
    }

    public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        }
        throw badArray(array);
    }

    public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof char[]) {
            return ((char[]) array)[index];
        }
        throw badArray(array);
    }

    public static short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        if (array instanceof byte[]) {
            return (short) ((byte[]) array)[index];
        }
        throw badArray(array);
    }

    public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof int[]) {
            return ((int[]) array)[index];
        }
        if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        }
        if (array instanceof char[]) {
            return ((char[]) array)[index];
        }
        if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    public static long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof long[]) {
            return ((long[]) array)[index];
        }
        if (array instanceof byte[]) {
            return (long) ((byte[]) array)[index];
        }
        if (array instanceof char[]) {
            return (long) ((char[]) array)[index];
        }
        if (array instanceof int[]) {
            return (long) ((int[]) array)[index];
        }
        if (array instanceof short[]) {
            return (long) ((short[]) array)[index];
        }
        throw badArray(array);
    }

    public static float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof float[]) {
            return ((float[]) array)[index];
        }
        if (array instanceof byte[]) {
            return (float) ((byte[]) array)[index];
        }
        if (array instanceof char[]) {
            return (float) ((char[]) array)[index];
        }
        if (array instanceof int[]) {
            return (float) ((int[]) array)[index];
        }
        if (array instanceof long[]) {
            return (float) ((long[]) array)[index];
        }
        if (array instanceof short[]) {
            return (float) ((short[]) array)[index];
        }
        throw badArray(array);
    }

    public static double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof double[]) {
            return ((double[]) array)[index];
        }
        if (array instanceof byte[]) {
            return (double) ((byte[]) array)[index];
        }
        if (array instanceof char[]) {
            return (double) ((char[]) array)[index];
        }
        if (array instanceof float[]) {
            return (double) ((float[]) array)[index];
        }
        if (array instanceof int[]) {
            return (double) ((int[]) array)[index];
        }
        if (array instanceof long[]) {
            return (double) ((long[]) array)[index];
        }
        if (array instanceof short[]) {
            return (double) ((short[]) array)[index];
        }
        throw badArray(array);
    }

    public static void set(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (!array.getClass().isArray()) {
            throw notAnArray(array);
        } else if (array instanceof Object[]) {
            if (value == null || array.getClass().getComponentType().isInstance(value)) {
                ((Object[]) array)[index] = value;
                return;
            }
            throw incompatibleType(array);
        } else if (value == null) {
            throw new IllegalArgumentException("Primitive array can't take null values.");
        } else if (value instanceof Boolean) {
            setBoolean(array, index, ((Boolean) value).booleanValue());
        } else if (value instanceof Byte) {
            setByte(array, index, ((Byte) value).byteValue());
        } else if (value instanceof Character) {
            setChar(array, index, ((Character) value).charValue());
        } else if (value instanceof Short) {
            setShort(array, index, ((Short) value).shortValue());
        } else if (value instanceof Integer) {
            setInt(array, index, ((Integer) value).intValue());
        } else if (value instanceof Long) {
            setLong(array, index, ((Long) value).longValue());
        } else if (value instanceof Float) {
            setFloat(array, index, ((Float) value).floatValue());
        } else if (value instanceof Double) {
            setDouble(array, index, ((Double) value).doubleValue());
        }
    }

    public static void setBoolean(Object array, int index, boolean z) {
        if (array instanceof boolean[]) {
            ((boolean[]) array)[index] = z;
            return;
        }
        throw badArray(array);
    }

    public static void setByte(Object array, int index, byte b) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof byte[]) {
            ((byte[]) array)[index] = b;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = (double) b;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = (float) b;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = b;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = (long) b;
        } else if (array instanceof short[]) {
            ((short[]) array)[index] = (short) b;
        } else {
            throw badArray(array);
        }
    }

    public static void setChar(Object array, int index, char c) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof char[]) {
            ((char[]) array)[index] = c;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = (double) c;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = (float) c;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = c;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = (long) c;
        } else {
            throw badArray(array);
        }
    }

    public static void setShort(Object array, int index, short s) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof short[]) {
            ((short[]) array)[index] = s;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = (double) s;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = (float) s;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = s;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = (long) s;
        } else {
            throw badArray(array);
        }
    }

    public static void setInt(Object array, int index, int i) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof int[]) {
            ((int[]) array)[index] = i;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = (double) i;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = (float) i;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = (long) i;
        } else {
            throw badArray(array);
        }
    }

    public static void setLong(Object array, int index, long l) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof long[]) {
            ((long[]) array)[index] = l;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = (double) l;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = (float) l;
        } else {
            throw badArray(array);
        }
    }

    public static void setFloat(Object array, int index, float f) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof float[]) {
            ((float[]) array)[index] = f;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = (double) f;
        } else {
            throw badArray(array);
        }
    }

    public static void setDouble(Object array, int index, double d) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof double[]) {
            ((double[]) array)[index] = d;
            return;
        }
        throw badArray(array);
    }

    private static Object newArray(Class<?> componentType, int length) throws NegativeArraySizeException {
        if (!componentType.isPrimitive()) {
            return createObjectArray(componentType, length);
        }
        if (componentType == Character.TYPE) {
            return new char[length];
        }
        if (componentType == Integer.TYPE) {
            return new int[length];
        }
        if (componentType == Byte.TYPE) {
            return new byte[length];
        }
        if (componentType == Boolean.TYPE) {
            return new boolean[length];
        }
        if (componentType == Short.TYPE) {
            return new short[length];
        }
        if (componentType == Long.TYPE) {
            return new long[length];
        }
        if (componentType == Float.TYPE) {
            return new float[length];
        }
        if (componentType == Double.TYPE) {
            return new double[length];
        }
        if (componentType == Void.TYPE) {
            throw new IllegalArgumentException("Can't allocate an array of void");
        }
        throw new AssertionError();
    }

    private static IllegalArgumentException notAnArray(Object o) {
        throw new IllegalArgumentException("Not an array: " + o.getClass());
    }

    private static IllegalArgumentException incompatibleType(Object o) {
        throw new IllegalArgumentException("Array has incompatible type: " + o.getClass());
    }

    private static RuntimeException badArray(Object array) {
        if (array == null) {
            throw new NullPointerException("array == null");
        } else if (!array.getClass().isArray()) {
            throw notAnArray(array);
        } else {
            throw incompatibleType(array);
        }
    }
}
