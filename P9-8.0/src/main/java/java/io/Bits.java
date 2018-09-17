package java.io;

class Bits {
    Bits() {
    }

    static boolean getBoolean(byte[] b, int off) {
        return b[off] != (byte) 0;
    }

    static char getChar(byte[] b, int off) {
        return (char) ((b[off + 1] & 255) + (b[off] << 8));
    }

    static short getShort(byte[] b, int off) {
        return (short) ((b[off + 1] & 255) + (b[off] << 8));
    }

    static int getInt(byte[] b, int off) {
        return (((b[off + 3] & 255) + ((b[off + 2] & 255) << 8)) + ((b[off + 1] & 255) << 16)) + (b[off] << 24);
    }

    static float getFloat(byte[] b, int off) {
        return Float.intBitsToFloat(getInt(b, off));
    }

    static long getLong(byte[] b, int off) {
        return (((((((((long) b[off + 7]) & 255) + ((((long) b[off + 6]) & 255) << 8)) + ((((long) b[off + 5]) & 255) << 16)) + ((((long) b[off + 4]) & 255) << 24)) + ((((long) b[off + 3]) & 255) << 32)) + ((((long) b[off + 2]) & 255) << 40)) + ((((long) b[off + 1]) & 255) << 48)) + (((long) b[off]) << 56);
    }

    static double getDouble(byte[] b, int off) {
        return Double.longBitsToDouble(getLong(b, off));
    }

    static void putBoolean(byte[] b, int off, boolean val) {
        b[off] = (byte) (val ? 1 : 0);
    }

    static void putChar(byte[] b, int off, char val) {
        b[off + 1] = (byte) val;
        b[off] = (byte) (val >>> 8);
    }

    static void putShort(byte[] b, int off, short val) {
        b[off + 1] = (byte) val;
        b[off] = (byte) (val >>> 8);
    }

    static void putInt(byte[] b, int off, int val) {
        b[off + 3] = (byte) val;
        b[off + 2] = (byte) (val >>> 8);
        b[off + 1] = (byte) (val >>> 16);
        b[off] = (byte) (val >>> 24);
    }

    static void putFloat(byte[] b, int off, float val) {
        putInt(b, off, Float.floatToIntBits(val));
    }

    static void putLong(byte[] b, int off, long val) {
        b[off + 7] = (byte) ((int) val);
        b[off + 6] = (byte) ((int) (val >>> 8));
        b[off + 5] = (byte) ((int) (val >>> 16));
        b[off + 4] = (byte) ((int) (val >>> 24));
        b[off + 3] = (byte) ((int) (val >>> 32));
        b[off + 2] = (byte) ((int) (val >>> 40));
        b[off + 1] = (byte) ((int) (val >>> 48));
        b[off] = (byte) ((int) (val >>> 56));
    }

    static void putDouble(byte[] b, int off, double val) {
        putLong(b, off, Double.doubleToLongBits(val));
    }
}
