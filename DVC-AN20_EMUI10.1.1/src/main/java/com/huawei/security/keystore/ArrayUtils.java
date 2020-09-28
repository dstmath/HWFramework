package com.huawei.security.keystore;

import java.util.Date;

public abstract class ArrayUtils {
    private ArrayUtils() {
    }

    static String[] nullToEmpty(String[] array) {
        return array != null ? array : EmptyArray.STRING;
    }

    static String[] cloneIfNotEmpty(String[] array) {
        return (array == null || array.length <= 0) ? array : (String[]) array.clone();
    }

    static byte[] cloneIfNotEmpty(byte[] array) {
        return (array == null || array.length <= 0) ? array : (byte[]) array.clone();
    }

    static Date cloneIfNotNull(Date value) {
        if (value != null) {
            return (Date) value.clone();
        }
        return null;
    }

    static byte[] cloneIfNotNull(byte[] value) {
        if (value != null) {
            return (byte[]) value.clone();
        }
        return null;
    }

    static byte[] concat(byte[] arr1, byte[] arr2) {
        return concat(new CopyArray(arr1, 0, arr1 != null ? arr1.length : 0), new CopyArray(arr2, 0, arr2 != null ? arr2.length : 0));
    }

    static byte[] concat(CopyArray array1, CopyArray array2) {
        if (array1.mArrayLen == 0) {
            return subArray(array2.mConcatArray, array2.mOffset, array2.mArrayLen);
        }
        if (array2.mArrayLen == 0) {
            return subArray(array1.mConcatArray, array1.mOffset, array1.mArrayLen);
        }
        byte[] result = new byte[(array1.mArrayLen + array2.mArrayLen)];
        System.arraycopy(array1.mConcatArray, array1.mOffset, result, 0, array1.mArrayLen);
        System.arraycopy(array2.mConcatArray, array2.mOffset, result, array1.mArrayLen, array2.mArrayLen);
        return result;
    }

    static byte[] subArray(byte[] arr, int offset, int len) {
        if (len == 0) {
            return EmptyArray.BYTE;
        }
        if (offset == 0 && len == arr.length) {
            return arr;
        }
        byte[] result = new byte[len];
        System.arraycopy(arr, offset, result, 0, len);
        return result;
    }

    static int[] concat(int[] arr1, int[] arr2) {
        if (arr1 == null || arr1.length == 0) {
            return arr2;
        }
        if (arr2 == null || arr2.length == 0) {
            return arr1;
        }
        int[] result = new int[(arr1.length + arr2.length)];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static class EmptyArray {
        public static final boolean[] BOOLEAN = new boolean[0];
        public static final byte[] BYTE = new byte[0];
        public static final char[] CHAR = new char[0];
        public static final Class<?>[] CLASS = new Class[0];
        public static final double[] DOUBLE = new double[0];
        public static final int[] INT = new int[0];
        public static final Object[] OBJECT = new Object[0];
        public static final StackTraceElement[] STACK_TRACE_ELEMENT = new StackTraceElement[0];
        public static final String[] STRING = new String[0];
        public static final Throwable[] THROWABLE = new Throwable[0];

        private EmptyArray() {
        }
    }

    public static final class CopyArray {
        private int mArrayLen;
        private byte[] mConcatArray;
        private int mOffset;

        CopyArray(byte[] concatArray, int offset, int arrayLen) {
            this.mConcatArray = concatArray;
            this.mOffset = offset;
            this.mArrayLen = arrayLen;
        }
    }
}
