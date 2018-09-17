package android.os;

import libcore.util.NativeAllocationRegistry;

public class HwBlob {
    private static final String TAG = "HwBlob";
    private static final NativeAllocationRegistry sNativeRegistry = new NativeAllocationRegistry(HwBlob.class.getClassLoader(), native_init(), 128);
    private long mNativeContext;

    private static final native long native_init();

    private final native void native_setup(int i);

    public final native boolean getBool(long j);

    public final native double getDouble(long j);

    public final native float getFloat(long j);

    public final native short getInt16(long j);

    public final native int getInt32(long j);

    public final native long getInt64(long j);

    public final native byte getInt8(long j);

    public final native String getString(long j);

    public final native long handle();

    public final native void putBlob(long j, HwBlob hwBlob);

    public final native void putBool(long j, boolean z);

    public final native void putDouble(long j, double d);

    public final native void putFloat(long j, float f);

    public final native void putInt16(long j, short s);

    public final native void putInt32(long j, int i);

    public final native void putInt64(long j, long j2);

    public final native void putInt8(long j, byte b);

    public final native void putString(long j, String str);

    public HwBlob(int size) {
        native_setup(size);
        sNativeRegistry.registerNativeAllocation(this, this.mNativeContext);
    }

    public static Boolean[] wrapArray(boolean[] array) {
        int n = array.length;
        Boolean[] wrappedArray = new Boolean[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Boolean.valueOf(array[i]);
        }
        return wrappedArray;
    }

    public static Long[] wrapArray(long[] array) {
        int n = array.length;
        Long[] wrappedArray = new Long[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Long.valueOf(array[i]);
        }
        return wrappedArray;
    }

    public static Byte[] wrapArray(byte[] array) {
        int n = array.length;
        Byte[] wrappedArray = new Byte[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Byte.valueOf(array[i]);
        }
        return wrappedArray;
    }

    public static Short[] wrapArray(short[] array) {
        int n = array.length;
        Short[] wrappedArray = new Short[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Short.valueOf(array[i]);
        }
        return wrappedArray;
    }

    public static Integer[] wrapArray(int[] array) {
        int n = array.length;
        Integer[] wrappedArray = new Integer[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Integer.valueOf(array[i]);
        }
        return wrappedArray;
    }

    public static Float[] wrapArray(float[] array) {
        int n = array.length;
        Float[] wrappedArray = new Float[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Float.valueOf(array[i]);
        }
        return wrappedArray;
    }

    public static Double[] wrapArray(double[] array) {
        int n = array.length;
        Double[] wrappedArray = new Double[n];
        for (int i = 0; i < n; i++) {
            wrappedArray[i] = Double.valueOf(array[i]);
        }
        return wrappedArray;
    }
}
