package libcore.io;

import java.nio.ByteOrder;

public final class Memory {
    public static native void memmove(Object obj, int i, Object obj2, int i2, long j);

    public static native byte peekByte(long j);

    public static native void peekByteArray(long j, byte[] bArr, int i, int i2);

    public static native void peekCharArray(long j, char[] cArr, int i, int i2, boolean z);

    public static native void peekDoubleArray(long j, double[] dArr, int i, int i2, boolean z);

    public static native void peekFloatArray(long j, float[] fArr, int i, int i2, boolean z);

    public static native void peekIntArray(long j, int[] iArr, int i, int i2, boolean z);

    private static native int peekIntNative(long j);

    public static native void peekLongArray(long j, long[] jArr, int i, int i2, boolean z);

    private static native long peekLongNative(long j);

    public static native void peekShortArray(long j, short[] sArr, int i, int i2, boolean z);

    private static native short peekShortNative(long j);

    public static native void pokeByte(long j, byte b);

    public static native void pokeByteArray(long j, byte[] bArr, int i, int i2);

    public static native void pokeCharArray(long j, char[] cArr, int i, int i2, boolean z);

    public static native void pokeDoubleArray(long j, double[] dArr, int i, int i2, boolean z);

    public static native void pokeFloatArray(long j, float[] fArr, int i, int i2, boolean z);

    public static native void pokeIntArray(long j, int[] iArr, int i, int i2, boolean z);

    private static native void pokeIntNative(long j, int i);

    public static native void pokeLongArray(long j, long[] jArr, int i, int i2, boolean z);

    private static native void pokeLongNative(long j, long j2);

    public static native void pokeShortArray(long j, short[] sArr, int i, int i2, boolean z);

    private static native void pokeShortNative(long j, short s);

    public static native void unsafeBulkGet(Object obj, int i, int i2, byte[] bArr, int i3, int i4, boolean z);

    public static native void unsafeBulkPut(byte[] bArr, int i, int i2, Object obj, int i3, int i4, boolean z);

    private Memory() {
    }

    public static int peekInt(byte[] src, int offset, ByteOrder order) {
        int offset2;
        if (order == ByteOrder.BIG_ENDIAN) {
            offset2 = offset + 1;
            offset = offset2 + 1;
            return ((((src[offset] & 255) << 24) | ((src[offset2] & 255) << 16)) | ((src[offset] & 255) << 8)) | ((src[offset + 1] & 255) << 0);
        }
        offset2 = offset + 1;
        offset = offset2 + 1;
        return ((((src[offset] & 255) << 0) | ((src[offset2] & 255) << 8)) | ((src[offset] & 255) << 16)) | ((src[offset + 1] & 255) << 24);
    }

    public static long peekLong(byte[] src, int offset, ByteOrder order) {
        int offset2;
        if (order == ByteOrder.BIG_ENDIAN) {
            offset2 = offset + 1;
            offset = offset2 + 1;
            offset2 = offset + 1;
            offset = offset2 + 1;
            int h = ((((src[offset] & 255) << 24) | ((src[offset2] & 255) << 16)) | ((src[offset] & 255) << 8)) | ((src[offset2] & 255) << 0);
            offset2 = offset + 1;
            offset = offset2 + 1;
            return (((long) h) << 32) | (((long) (((((src[offset] & 255) << 24) | ((src[offset2] & 255) << 16)) | ((src[offset] & 255) << 8)) | ((src[offset + 1] & 255) << 0))) & 4294967295L);
        }
        offset2 = offset + 1;
        offset = offset2 + 1;
        offset2 = offset + 1;
        offset = offset2 + 1;
        int l = ((((src[offset] & 255) << 0) | ((src[offset2] & 255) << 8)) | ((src[offset] & 255) << 16)) | ((src[offset2] & 255) << 24);
        offset2 = offset + 1;
        offset = offset2 + 1;
        return (((long) (((((src[offset] & 255) << 0) | ((src[offset2] & 255) << 8)) | ((src[offset] & 255) << 16)) | ((src[offset + 1] & 255) << 24))) << 32) | (((long) l) & 4294967295L);
    }

    public static short peekShort(byte[] src, int offset, ByteOrder order) {
        if (order == ByteOrder.BIG_ENDIAN) {
            return (short) ((src[offset] << 8) | (src[offset + 1] & 255));
        }
        return (short) ((src[offset + 1] << 8) | (src[offset] & 255));
    }

    public static void pokeInt(byte[] dst, int offset, int value, ByteOrder order) {
        int i;
        if (order == ByteOrder.BIG_ENDIAN) {
            i = offset + 1;
            dst[offset] = (byte) ((value >> 24) & 255);
            offset = i + 1;
            dst[i] = (byte) ((value >> 16) & 255);
            i = offset + 1;
            dst[offset] = (byte) ((value >> 8) & 255);
            dst[i] = (byte) ((value >> 0) & 255);
            offset = i;
            return;
        }
        i = offset + 1;
        dst[offset] = (byte) ((value >> 0) & 255);
        offset = i + 1;
        dst[i] = (byte) ((value >> 8) & 255);
        i = offset + 1;
        dst[offset] = (byte) ((value >> 16) & 255);
        dst[i] = (byte) ((value >> 24) & 255);
        offset = i;
    }

    public static void pokeLong(byte[] dst, int offset, long value, ByteOrder order) {
        int i;
        int i2;
        if (order == ByteOrder.BIG_ENDIAN) {
            i = (int) (value >> 32);
            i2 = offset + 1;
            dst[offset] = (byte) ((i >> 24) & 255);
            offset = i2 + 1;
            dst[i2] = (byte) ((i >> 16) & 255);
            i2 = offset + 1;
            dst[offset] = (byte) ((i >> 8) & 255);
            offset = i2 + 1;
            dst[i2] = (byte) ((i >> 0) & 255);
            i = (int) value;
            i2 = offset + 1;
            dst[offset] = (byte) ((i >> 24) & 255);
            offset = i2 + 1;
            dst[i2] = (byte) ((i >> 16) & 255);
            i2 = offset + 1;
            dst[offset] = (byte) ((i >> 8) & 255);
            dst[i2] = (byte) ((i >> 0) & 255);
            offset = i2;
            return;
        }
        i = (int) value;
        i2 = offset + 1;
        dst[offset] = (byte) ((i >> 0) & 255);
        offset = i2 + 1;
        dst[i2] = (byte) ((i >> 8) & 255);
        i2 = offset + 1;
        dst[offset] = (byte) ((i >> 16) & 255);
        offset = i2 + 1;
        dst[i2] = (byte) ((i >> 24) & 255);
        i = (int) (value >> 32);
        i2 = offset + 1;
        dst[offset] = (byte) ((i >> 0) & 255);
        offset = i2 + 1;
        dst[i2] = (byte) ((i >> 8) & 255);
        i2 = offset + 1;
        dst[offset] = (byte) ((i >> 16) & 255);
        dst[i2] = (byte) ((i >> 24) & 255);
        offset = i2;
    }

    public static void pokeShort(byte[] dst, int offset, short value, ByteOrder order) {
        int offset2;
        if (order == ByteOrder.BIG_ENDIAN) {
            offset2 = offset + 1;
            dst[offset] = (byte) ((value >> 8) & 255);
            dst[offset2] = (byte) ((value >> 0) & 255);
            offset = offset2;
            return;
        }
        offset2 = offset + 1;
        dst[offset] = (byte) ((value >> 0) & 255);
        dst[offset2] = (byte) ((value >> 8) & 255);
        offset = offset2;
    }

    public static int peekInt(long address, boolean swap) {
        int result = peekIntNative(address);
        if (swap) {
            return Integer.reverseBytes(result);
        }
        return result;
    }

    public static long peekLong(long address, boolean swap) {
        long result = peekLongNative(address);
        if (swap) {
            return Long.reverseBytes(result);
        }
        return result;
    }

    public static short peekShort(long address, boolean swap) {
        short result = peekShortNative(address);
        if (swap) {
            return Short.reverseBytes(result);
        }
        return result;
    }

    public static void pokeInt(long address, int value, boolean swap) {
        if (swap) {
            value = Integer.reverseBytes(value);
        }
        pokeIntNative(address, value);
    }

    public static void pokeLong(long address, long value, boolean swap) {
        if (swap) {
            value = Long.reverseBytes(value);
        }
        pokeLongNative(address, value);
    }

    public static void pokeShort(long address, short value, boolean swap) {
        if (swap) {
            value = Short.reverseBytes(value);
        }
        pokeShortNative(address, value);
    }
}
