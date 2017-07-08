package com.android.internal.util;

import android.util.Log;

public final class GrowingArrayUtils {
    static final /* synthetic */ boolean -assertionsDisabled = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.GrowingArrayUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.GrowingArrayUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.GrowingArrayUtils.<clinit>():void");
    }

    public static <T> T[] append(T[] array, int currentSize, T element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            T[] newArray = ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static int[] append(int[] array, int currentSize, int element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static long[] append(long[] array, int currentSize, long element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static boolean[] append(boolean[] array, int currentSize, boolean element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static float[] append(float[] array, int currentSize, float element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 > array.length) {
            float[] newArray = ArrayUtils.newUnpaddedFloatArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static <T> T[] insert(T[] array, int currentSize, int index, T element) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (currentSize <= array.length) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        try {
            if (currentSize + 1 <= array.length) {
                System.arraycopy(array, index, array, index + 1, currentSize - index);
                array[index] = element;
                return array;
            }
            T[] newArray = ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("GrowingArrayUtils", "System.arraycopy error");
            return array;
        }
    }

    public static int[] insert(int[] array, int currentSize, int index, int element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public static long[] insert(long[] array, int currentSize, int index, long element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
        if (!-assertionsDisabled) {
            if ((currentSize <= array.length ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }

    private GrowingArrayUtils() {
    }
}
