package java.nio;

import java.security.AccessController;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.security.action.GetPropertyAction;

class Bits {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final int JNI_COPY_FROM_ARRAY_THRESHOLD = 6;
    static final int JNI_COPY_TO_ARRAY_THRESHOLD = 6;
    static final long UNSAFE_COPY_THRESHOLD = 1048576;
    private static final ByteOrder byteOrder = null;
    private static volatile long count;
    private static volatile long maxMemory;
    private static boolean memoryLimitSet;
    private static int pageSize;
    private static volatile long reservedMemory;
    private static volatile long totalCapacity;
    private static boolean unaligned;
    private static boolean unalignedKnown;
    private static final Unsafe unsafe = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.Bits.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.Bits.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.Bits.<clinit>():void");
    }

    static native void copyFromIntArray(Object obj, long j, long j2, long j3);

    static native void copyFromLongArray(Object obj, long j, long j2, long j3);

    static native void copyFromShortArray(Object obj, long j, long j2, long j3);

    static native void copyToIntArray(long j, Object obj, long j2, long j3);

    static native void copyToLongArray(long j, Object obj, long j2, long j3);

    static native void copyToShortArray(long j, Object obj, long j2, long j3);

    private Bits() {
    }

    static short swap(short x) {
        return Short.reverseBytes(x);
    }

    static char swap(char x) {
        return Character.reverseBytes(x);
    }

    static int swap(int x) {
        return Integer.reverseBytes(x);
    }

    static long swap(long x) {
        return Long.reverseBytes(x);
    }

    private static char makeChar(byte b1, byte b0) {
        return (char) ((b1 << 8) | (b0 & 255));
    }

    static char getCharL(ByteBuffer bb, int bi) {
        return makeChar(bb._get(bi + 1), bb._get(bi));
    }

    static char getCharL(long a) {
        return makeChar(_get(1 + a), _get(a));
    }

    static char getCharB(ByteBuffer bb, int bi) {
        return makeChar(bb._get(bi), bb._get(bi + 1));
    }

    static char getCharB(long a) {
        return makeChar(_get(a), _get(1 + a));
    }

    static char getChar(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getCharB(bb, bi) : getCharL(bb, bi);
    }

    static char getChar(long a, boolean bigEndian) {
        return bigEndian ? getCharB(a) : getCharL(a);
    }

    private static byte char1(char x) {
        return (byte) (x >> 8);
    }

    private static byte char0(char x) {
        return (byte) x;
    }

    static void putCharL(ByteBuffer bb, int bi, char x) {
        bb._put(bi, char0(x));
        bb._put(bi + 1, char1(x));
    }

    static void putCharL(long a, char x) {
        _put(a, char0(x));
        _put(1 + a, char1(x));
    }

    static void putCharB(ByteBuffer bb, int bi, char x) {
        bb._put(bi, char1(x));
        bb._put(bi + 1, char0(x));
    }

    static void putCharB(long a, char x) {
        _put(a, char1(x));
        _put(1 + a, char0(x));
    }

    static void putChar(ByteBuffer bb, int bi, char x, boolean bigEndian) {
        if (bigEndian) {
            putCharB(bb, bi, x);
        } else {
            putCharL(bb, bi, x);
        }
    }

    static void putChar(long a, char x, boolean bigEndian) {
        if (bigEndian) {
            putCharB(a, x);
        } else {
            putCharL(a, x);
        }
    }

    private static short makeShort(byte b1, byte b0) {
        return (short) ((b1 << 8) | (b0 & 255));
    }

    static short getShortL(ByteBuffer bb, int bi) {
        return makeShort(bb._get(bi + 1), bb._get(bi));
    }

    static short getShortL(long a) {
        return makeShort(_get(1 + a), _get(a));
    }

    static short getShortB(ByteBuffer bb, int bi) {
        return makeShort(bb._get(bi), bb._get(bi + 1));
    }

    static short getShortB(long a) {
        return makeShort(_get(a), _get(1 + a));
    }

    static short getShort(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getShortB(bb, bi) : getShortL(bb, bi);
    }

    static short getShort(long a, boolean bigEndian) {
        return bigEndian ? getShortB(a) : getShortL(a);
    }

    private static byte short1(short x) {
        return (byte) (x >> 8);
    }

    private static byte short0(short x) {
        return (byte) x;
    }

    static void putShortL(ByteBuffer bb, int bi, short x) {
        bb._put(bi, short0(x));
        bb._put(bi + 1, short1(x));
    }

    static void putShortL(long a, short x) {
        _put(a, short0(x));
        _put(1 + a, short1(x));
    }

    static void putShortB(ByteBuffer bb, int bi, short x) {
        bb._put(bi, short1(x));
        bb._put(bi + 1, short0(x));
    }

    static void putShortB(long a, short x) {
        _put(a, short1(x));
        _put(1 + a, short0(x));
    }

    static void putShort(ByteBuffer bb, int bi, short x, boolean bigEndian) {
        if (bigEndian) {
            putShortB(bb, bi, x);
        } else {
            putShortL(bb, bi, x);
        }
    }

    static void putShort(long a, short x, boolean bigEndian) {
        if (bigEndian) {
            putShortB(a, x);
        } else {
            putShortL(a, x);
        }
    }

    private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3 << 24) | ((b2 & 255) << 16)) | ((b1 & 255) << 8)) | (b0 & 255);
    }

    static int getIntL(ByteBuffer bb, int bi) {
        return makeInt(bb._get(bi + 3), bb._get(bi + 2), bb._get(bi + 1), bb._get(bi));
    }

    static int getIntL(long a) {
        return makeInt(_get(3 + a), _get(2 + a), _get(1 + a), _get(a));
    }

    static int getIntB(ByteBuffer bb, int bi) {
        return makeInt(bb._get(bi), bb._get(bi + 1), bb._get(bi + 2), bb._get(bi + 3));
    }

    static int getIntB(long a) {
        return makeInt(_get(a), _get(1 + a), _get(2 + a), _get(3 + a));
    }

    static int getInt(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getIntB(bb, bi) : getIntL(bb, bi);
    }

    static int getInt(long a, boolean bigEndian) {
        return bigEndian ? getIntB(a) : getIntL(a);
    }

    private static byte int3(int x) {
        return (byte) (x >> 24);
    }

    private static byte int2(int x) {
        return (byte) (x >> 16);
    }

    private static byte int1(int x) {
        return (byte) (x >> 8);
    }

    private static byte int0(int x) {
        return (byte) x;
    }

    static void putIntL(ByteBuffer bb, int bi, int x) {
        bb._put(bi + 3, int3(x));
        bb._put(bi + 2, int2(x));
        bb._put(bi + 1, int1(x));
        bb._put(bi, int0(x));
    }

    static void putIntL(long a, int x) {
        _put(3 + a, int3(x));
        _put(2 + a, int2(x));
        _put(1 + a, int1(x));
        _put(a, int0(x));
    }

    static void putIntB(ByteBuffer bb, int bi, int x) {
        bb._put(bi, int3(x));
        bb._put(bi + 1, int2(x));
        bb._put(bi + 2, int1(x));
        bb._put(bi + 3, int0(x));
    }

    static void putIntB(long a, int x) {
        _put(a, int3(x));
        _put(1 + a, int2(x));
        _put(2 + a, int1(x));
        _put(3 + a, int0(x));
    }

    static void putInt(ByteBuffer bb, int bi, int x, boolean bigEndian) {
        if (bigEndian) {
            putIntB(bb, bi, x);
        } else {
            putIntL(bb, bi, x);
        }
    }

    static void putInt(long a, int x, boolean bigEndian) {
        if (bigEndian) {
            putIntB(a, x);
        } else {
            putIntL(a, x);
        }
    }

    private static long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
        return (((((((((long) b7) << 56) | ((((long) b6) & 255) << 48)) | ((((long) b5) & 255) << 40)) | ((((long) b4) & 255) << 32)) | ((((long) b3) & 255) << 24)) | ((((long) b2) & 255) << 16)) | ((((long) b1) & 255) << 8)) | (((long) b0) & 255);
    }

    static long getLongL(ByteBuffer bb, int bi) {
        return makeLong(bb._get(bi + 7), bb._get(bi + JNI_COPY_TO_ARRAY_THRESHOLD), bb._get(bi + 5), bb._get(bi + 4), bb._get(bi + 3), bb._get(bi + 2), bb._get(bi + 1), bb._get(bi));
    }

    static long getLongL(long a) {
        return makeLong(_get(7 + a), _get(6 + a), _get(5 + a), _get(4 + a), _get(3 + a), _get(2 + a), _get(1 + a), _get(a));
    }

    static long getLongB(ByteBuffer bb, int bi) {
        return makeLong(bb._get(bi), bb._get(bi + 1), bb._get(bi + 2), bb._get(bi + 3), bb._get(bi + 4), bb._get(bi + 5), bb._get(bi + JNI_COPY_TO_ARRAY_THRESHOLD), bb._get(bi + 7));
    }

    static long getLongB(long a) {
        return makeLong(_get(a), _get(1 + a), _get(2 + a), _get(3 + a), _get(4 + a), _get(5 + a), _get(6 + a), _get(7 + a));
    }

    static long getLong(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getLongB(bb, bi) : getLongL(bb, bi);
    }

    static long getLong(long a, boolean bigEndian) {
        return bigEndian ? getLongB(a) : getLongL(a);
    }

    private static byte long7(long x) {
        return (byte) ((int) (x >> 56));
    }

    private static byte long6(long x) {
        return (byte) ((int) (x >> 48));
    }

    private static byte long5(long x) {
        return (byte) ((int) (x >> 40));
    }

    private static byte long4(long x) {
        return (byte) ((int) (x >> 32));
    }

    private static byte long3(long x) {
        return (byte) ((int) (x >> 24));
    }

    private static byte long2(long x) {
        return (byte) ((int) (x >> 16));
    }

    private static byte long1(long x) {
        return (byte) ((int) (x >> 8));
    }

    private static byte long0(long x) {
        return (byte) ((int) x);
    }

    static void putLongL(ByteBuffer bb, int bi, long x) {
        bb._put(bi + 7, long7(x));
        bb._put(bi + JNI_COPY_TO_ARRAY_THRESHOLD, long6(x));
        bb._put(bi + 5, long5(x));
        bb._put(bi + 4, long4(x));
        bb._put(bi + 3, long3(x));
        bb._put(bi + 2, long2(x));
        bb._put(bi + 1, long1(x));
        bb._put(bi, long0(x));
    }

    static void putLongL(long a, long x) {
        _put(7 + a, long7(x));
        _put(6 + a, long6(x));
        _put(5 + a, long5(x));
        _put(4 + a, long4(x));
        _put(3 + a, long3(x));
        _put(2 + a, long2(x));
        _put(1 + a, long1(x));
        _put(a, long0(x));
    }

    static void putLongB(ByteBuffer bb, int bi, long x) {
        bb._put(bi, long7(x));
        bb._put(bi + 1, long6(x));
        bb._put(bi + 2, long5(x));
        bb._put(bi + 3, long4(x));
        bb._put(bi + 4, long3(x));
        bb._put(bi + 5, long2(x));
        bb._put(bi + JNI_COPY_TO_ARRAY_THRESHOLD, long1(x));
        bb._put(bi + 7, long0(x));
    }

    static void putLongB(long a, long x) {
        _put(a, long7(x));
        _put(1 + a, long6(x));
        _put(2 + a, long5(x));
        _put(3 + a, long4(x));
        _put(4 + a, long3(x));
        _put(5 + a, long2(x));
        _put(6 + a, long1(x));
        _put(7 + a, long0(x));
    }

    static void putLong(ByteBuffer bb, int bi, long x, boolean bigEndian) {
        if (bigEndian) {
            putLongB(bb, bi, x);
        } else {
            putLongL(bb, bi, x);
        }
    }

    static void putLong(long a, long x, boolean bigEndian) {
        if (bigEndian) {
            putLongB(a, x);
        } else {
            putLongL(a, x);
        }
    }

    static float getFloatL(ByteBuffer bb, int bi) {
        return Float.intBitsToFloat(getIntL(bb, bi));
    }

    static float getFloatL(long a) {
        return Float.intBitsToFloat(getIntL(a));
    }

    static float getFloatB(ByteBuffer bb, int bi) {
        return Float.intBitsToFloat(getIntB(bb, bi));
    }

    static float getFloatB(long a) {
        return Float.intBitsToFloat(getIntB(a));
    }

    static float getFloat(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getFloatB(bb, bi) : getFloatL(bb, bi);
    }

    static float getFloat(long a, boolean bigEndian) {
        return bigEndian ? getFloatB(a) : getFloatL(a);
    }

    static void putFloatL(ByteBuffer bb, int bi, float x) {
        putIntL(bb, bi, Float.floatToRawIntBits(x));
    }

    static void putFloatL(long a, float x) {
        putIntL(a, Float.floatToRawIntBits(x));
    }

    static void putFloatB(ByteBuffer bb, int bi, float x) {
        putIntB(bb, bi, Float.floatToRawIntBits(x));
    }

    static void putFloatB(long a, float x) {
        putIntB(a, Float.floatToRawIntBits(x));
    }

    static void putFloat(ByteBuffer bb, int bi, float x, boolean bigEndian) {
        if (bigEndian) {
            putFloatB(bb, bi, x);
        } else {
            putFloatL(bb, bi, x);
        }
    }

    static void putFloat(long a, float x, boolean bigEndian) {
        if (bigEndian) {
            putFloatB(a, x);
        } else {
            putFloatL(a, x);
        }
    }

    static double getDoubleL(ByteBuffer bb, int bi) {
        return Double.longBitsToDouble(getLongL(bb, bi));
    }

    static double getDoubleL(long a) {
        return Double.longBitsToDouble(getLongL(a));
    }

    static double getDoubleB(ByteBuffer bb, int bi) {
        return Double.longBitsToDouble(getLongB(bb, bi));
    }

    static double getDoubleB(long a) {
        return Double.longBitsToDouble(getLongB(a));
    }

    static double getDouble(ByteBuffer bb, int bi, boolean bigEndian) {
        return bigEndian ? getDoubleB(bb, bi) : getDoubleL(bb, bi);
    }

    static double getDouble(long a, boolean bigEndian) {
        return bigEndian ? getDoubleB(a) : getDoubleL(a);
    }

    static void putDoubleL(ByteBuffer bb, int bi, double x) {
        putLongL(bb, bi, Double.doubleToRawLongBits(x));
    }

    static void putDoubleL(long a, double x) {
        putLongL(a, Double.doubleToRawLongBits(x));
    }

    static void putDoubleB(ByteBuffer bb, int bi, double x) {
        putLongB(bb, bi, Double.doubleToRawLongBits(x));
    }

    static void putDoubleB(long a, double x) {
        putLongB(a, Double.doubleToRawLongBits(x));
    }

    static void putDouble(ByteBuffer bb, int bi, double x, boolean bigEndian) {
        if (bigEndian) {
            putDoubleB(bb, bi, x);
        } else {
            putDoubleL(bb, bi, x);
        }
    }

    static void putDouble(long a, double x, boolean bigEndian) {
        if (bigEndian) {
            putDoubleB(a, x);
        } else {
            putDoubleL(a, x);
        }
    }

    private static byte _get(long a) {
        return unsafe.getByte(a);
    }

    private static void _put(long a, byte b) {
        unsafe.putByte(a, b);
    }

    static Unsafe unsafe() {
        return unsafe;
    }

    static ByteOrder byteOrder() {
        return byteOrder;
    }

    static int pageSize() {
        if (pageSize == -1) {
            pageSize = unsafe().pageSize();
        }
        return pageSize;
    }

    static int pageCount(long size) {
        return ((int) ((((long) pageSize()) + size) - 1)) / pageSize();
    }

    static boolean unaligned() {
        if (unalignedKnown) {
            return unaligned;
        }
        boolean z;
        String arch = (String) AccessController.doPrivileged(new GetPropertyAction("os.arch"));
        if (arch.equals("i386") || arch.equals("x86") || arch.equals("amd64")) {
            z = true;
        } else {
            z = arch.equals("x86_64");
        }
        unaligned = z;
        unalignedKnown = true;
        return unaligned;
    }

    static void reserveMemory(long size, int cap) {
        synchronized (Bits.class) {
            if (!memoryLimitSet && VM.isBooted()) {
                maxMemory = VM.maxDirectMemory();
                memoryLimitSet = true;
            }
            if (((long) cap) <= maxMemory - totalCapacity) {
                reservedMemory += size;
                totalCapacity += (long) cap;
                count++;
                return;
            }
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (Bits.class) {
                if (totalCapacity + ((long) cap) > maxMemory) {
                    throw new OutOfMemoryError("Direct buffer memory");
                }
                reservedMemory += size;
                totalCapacity += (long) cap;
                count++;
            }
        }
    }

    static synchronized void unreserveMemory(long size, int cap) {
        synchronized (Bits.class) {
            if (reservedMemory > 0) {
                reservedMemory -= size;
                totalCapacity -= (long) cap;
                count--;
                if (!-assertionsDisabled) {
                    if ((reservedMemory > -1 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
            }
        }
    }

    static void copyFromArray(Object src, long srcBaseOffset, long srcPos, long dstAddr, long length) {
        long offset = srcBaseOffset + srcPos;
        while (length > 0) {
            long size = length > UNSAFE_COPY_THRESHOLD ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemoryFromPrimitiveArray(src, offset, dstAddr, size);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }

    static void copyToArray(long srcAddr, Object dst, long dstBaseOffset, long dstPos, long length) {
        long offset = dstBaseOffset + dstPos;
        while (length > 0) {
            long size = length > UNSAFE_COPY_THRESHOLD ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemoryToPrimitiveArray(srcAddr, dst, offset, size);
            length -= size;
            srcAddr += size;
            offset += size;
        }
    }

    static void copyFromCharArray(Object src, long srcPos, long dstAddr, long length) {
        copyFromShortArray(src, srcPos, dstAddr, length);
    }

    static void copyToCharArray(long srcAddr, Object dst, long dstPos, long length) {
        copyToShortArray(srcAddr, dst, dstPos, length);
    }
}
