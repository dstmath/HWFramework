package java.lang;

public final class Short extends Number implements Comparable<Short> {
    public static final int BYTES = 2;
    public static final short MAX_VALUE = Short.MAX_VALUE;
    public static final short MIN_VALUE = Short.MIN_VALUE;
    public static final int SIZE = 16;
    public static final Class<Short> TYPE = short[].class.getComponentType();
    private static final long serialVersionUID = 7515723908773894738L;
    private final short value;

    private static class ShortCache {
        static final Short[] cache = new Short[256];

        private ShortCache() {
        }

        static {
            for (int i = 0; i < cache.length; i++) {
                cache[i] = new Short((short) (i - 128));
            }
        }
    }

    public static String toString(short s) {
        return Integer.toString(s, 10);
    }

    public static short parseShort(String s, int radix) throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i >= -32768 && i <= 32767) {
            return (short) i;
        }
        throw new NumberFormatException("Value out of range. Value:\"" + s + "\" Radix:" + radix);
    }

    public static short parseShort(String s) throws NumberFormatException {
        return parseShort(s, 10);
    }

    public static Short valueOf(String s, int radix) throws NumberFormatException {
        return valueOf(parseShort(s, radix));
    }

    public static Short valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    public static Short valueOf(short s) {
        short sAsInt = s;
        if (sAsInt < (short) -128 || sAsInt > (short) 127) {
            return new Short(s);
        }
        return ShortCache.cache[sAsInt + 128];
    }

    public static Short decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm).intValue();
        if (i >= -32768 && i <= 32767) {
            return valueOf((short) i);
        }
        throw new NumberFormatException("Value " + i + " out of range from input " + nm);
    }

    public Short(short value) {
        this.value = value;
    }

    public Short(String s) throws NumberFormatException {
        this.value = parseShort(s, 10);
    }

    public byte byteValue() {
        return (byte) this.value;
    }

    public short shortValue() {
        return this.value;
    }

    public int intValue() {
        return this.value;
    }

    public long longValue() {
        return (long) this.value;
    }

    public float floatValue() {
        return (float) this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public String toString() {
        return Integer.toString(this.value);
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(short value) {
        return value;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Short)) {
            return false;
        }
        if (this.value == ((Short) obj).shortValue()) {
            z = true;
        }
        return z;
    }

    public int compareTo(Short anotherShort) {
        return compare(this.value, anotherShort.value);
    }

    public static int compare(short x, short y) {
        return x - y;
    }

    public static short reverseBytes(short i) {
        return (short) (((65280 & i) >> 8) | (i << 8));
    }

    public static int toUnsignedInt(short x) {
        return 65535 & x;
    }

    public static long toUnsignedLong(short x) {
        return ((long) x) & 65535;
    }
}
