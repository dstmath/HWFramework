package org.bouncycastle.util;

import java.math.BigInteger;
import java.util.NoSuchElementException;

public final class Arrays {

    public static class Iterator<T> implements java.util.Iterator<T> {
        private final T[] dataArray;
        private int position = 0;

        public Iterator(T[] tArr) {
            this.dataArray = tArr;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.position < this.dataArray.length;
        }

        @Override // java.util.Iterator
        public T next() {
            int i = this.position;
            T[] tArr = this.dataArray;
            if (i != tArr.length) {
                this.position = i + 1;
                return tArr[i];
            }
            throw new NoSuchElementException("Out of elements: " + this.position);
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from an Array.");
        }
    }

    private Arrays() {
    }

    public static byte[] append(byte[] bArr, byte b) {
        if (bArr == null) {
            return new byte[]{b};
        }
        int length = bArr.length;
        byte[] bArr2 = new byte[(length + 1)];
        System.arraycopy(bArr, 0, bArr2, 0, length);
        bArr2[length] = b;
        return bArr2;
    }

    public static int[] append(int[] iArr, int i) {
        if (iArr == null) {
            return new int[]{i};
        }
        int length = iArr.length;
        int[] iArr2 = new int[(length + 1)];
        System.arraycopy(iArr, 0, iArr2, 0, length);
        iArr2[length] = i;
        return iArr2;
    }

    public static String[] append(String[] strArr, String str) {
        if (strArr == null) {
            return new String[]{str};
        }
        int length = strArr.length;
        String[] strArr2 = new String[(length + 1)];
        System.arraycopy(strArr, 0, strArr2, 0, length);
        strArr2[length] = str;
        return strArr2;
    }

    public static short[] append(short[] sArr, short s) {
        if (sArr == null) {
            return new short[]{s};
        }
        int length = sArr.length;
        short[] sArr2 = new short[(length + 1)];
        System.arraycopy(sArr, 0, sArr2, 0, length);
        sArr2[length] = s;
        return sArr2;
    }

    public static boolean areAllZeroes(byte[] bArr, int i, int i2) {
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            i3 |= bArr[i + i4];
        }
        return i3 == 0;
    }

    public static boolean areEqual(byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) {
        int i5 = i2 - i;
        if (i5 != i4 - i3) {
            return false;
        }
        for (int i6 = 0; i6 < i5; i6++) {
            if (bArr[i + i6] != bArr2[i3 + i6]) {
                return false;
            }
        }
        return true;
    }

    public static boolean areEqual(byte[] bArr, byte[] bArr2) {
        return java.util.Arrays.equals(bArr, bArr2);
    }

    public static boolean areEqual(char[] cArr, char[] cArr2) {
        return java.util.Arrays.equals(cArr, cArr2);
    }

    public static boolean areEqual(int[] iArr, int[] iArr2) {
        return java.util.Arrays.equals(iArr, iArr2);
    }

    public static boolean areEqual(long[] jArr, long[] jArr2) {
        return java.util.Arrays.equals(jArr, jArr2);
    }

    public static boolean areEqual(Object[] objArr, Object[] objArr2) {
        return java.util.Arrays.equals(objArr, objArr2);
    }

    public static boolean areEqual(short[] sArr, short[] sArr2) {
        return java.util.Arrays.equals(sArr, sArr2);
    }

    public static boolean areEqual(boolean[] zArr, boolean[] zArr2) {
        return java.util.Arrays.equals(zArr, zArr2);
    }

    public static void clear(byte[] bArr) {
        if (bArr != null) {
            java.util.Arrays.fill(bArr, (byte) 0);
        }
    }

    public static void clear(int[] iArr) {
        if (iArr != null) {
            java.util.Arrays.fill(iArr, 0);
        }
    }

    public static byte[] clone(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public static byte[] clone(byte[] bArr, byte[] bArr2) {
        if (bArr == null) {
            return null;
        }
        if (bArr2 == null || bArr2.length != bArr.length) {
            return clone(bArr);
        }
        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        return bArr2;
    }

    public static char[] clone(char[] cArr) {
        if (cArr == null) {
            return null;
        }
        return (char[]) cArr.clone();
    }

    public static int[] clone(int[] iArr) {
        if (iArr == null) {
            return null;
        }
        return (int[]) iArr.clone();
    }

    public static long[] clone(long[] jArr) {
        if (jArr == null) {
            return null;
        }
        return (long[]) jArr.clone();
    }

    public static long[] clone(long[] jArr, long[] jArr2) {
        if (jArr == null) {
            return null;
        }
        if (jArr2 == null || jArr2.length != jArr.length) {
            return clone(jArr);
        }
        System.arraycopy(jArr, 0, jArr2, 0, jArr2.length);
        return jArr2;
    }

    public static BigInteger[] clone(BigInteger[] bigIntegerArr) {
        if (bigIntegerArr == null) {
            return null;
        }
        return (BigInteger[]) bigIntegerArr.clone();
    }

    public static short[] clone(short[] sArr) {
        if (sArr == null) {
            return null;
        }
        return (short[]) sArr.clone();
    }

    public static boolean[] clone(boolean[] zArr) {
        if (zArr == null) {
            return null;
        }
        return (boolean[]) zArr.clone();
    }

    public static byte[][] clone(byte[][] bArr) {
        if (bArr == null) {
            return null;
        }
        byte[][] bArr2 = new byte[bArr.length][];
        for (int i = 0; i != bArr2.length; i++) {
            bArr2[i] = clone(bArr[i]);
        }
        return bArr2;
    }

    public static byte[][][] clone(byte[][][] bArr) {
        if (bArr == null) {
            return null;
        }
        byte[][][] bArr2 = new byte[bArr.length][][];
        for (int i = 0; i != bArr2.length; i++) {
            bArr2[i] = clone(bArr[i]);
        }
        return bArr2;
    }

    public static int compareUnsigned(byte[] bArr, byte[] bArr2) {
        if (bArr == bArr2) {
            return 0;
        }
        if (bArr == null) {
            return -1;
        }
        if (bArr2 == null) {
            return 1;
        }
        int min = Math.min(bArr.length, bArr2.length);
        for (int i = 0; i < min; i++) {
            int i2 = bArr[i] & 255;
            int i3 = bArr2[i] & 255;
            if (i2 < i3) {
                return -1;
            }
            if (i2 > i3) {
                return 1;
            }
        }
        if (bArr.length < bArr2.length) {
            return -1;
        }
        return bArr.length > bArr2.length ? 1 : 0;
    }

    public static byte[] concatenate(byte[] bArr, byte[] bArr2) {
        if (bArr == null) {
            return clone(bArr2);
        }
        if (bArr2 == null) {
            return clone(bArr);
        }
        byte[] bArr3 = new byte[(bArr.length + bArr2.length)];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return bArr3;
    }

    public static byte[] concatenate(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        if (bArr == null) {
            return concatenate(bArr2, bArr3);
        }
        if (bArr2 == null) {
            return concatenate(bArr, bArr3);
        }
        if (bArr3 == null) {
            return concatenate(bArr, bArr2);
        }
        byte[] bArr4 = new byte[(bArr.length + bArr2.length + bArr3.length)];
        System.arraycopy(bArr, 0, bArr4, 0, bArr.length);
        int length = bArr.length + 0;
        System.arraycopy(bArr2, 0, bArr4, length, bArr2.length);
        System.arraycopy(bArr3, 0, bArr4, length + bArr2.length, bArr3.length);
        return bArr4;
    }

    public static byte[] concatenate(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        if (bArr == null) {
            return concatenate(bArr2, bArr3, bArr4);
        }
        if (bArr2 == null) {
            return concatenate(bArr, bArr3, bArr4);
        }
        if (bArr3 == null) {
            return concatenate(bArr, bArr2, bArr4);
        }
        if (bArr4 == null) {
            return concatenate(bArr, bArr2, bArr3);
        }
        byte[] bArr5 = new byte[(bArr.length + bArr2.length + bArr3.length + bArr4.length)];
        System.arraycopy(bArr, 0, bArr5, 0, bArr.length);
        int length = bArr.length + 0;
        System.arraycopy(bArr2, 0, bArr5, length, bArr2.length);
        int length2 = length + bArr2.length;
        System.arraycopy(bArr3, 0, bArr5, length2, bArr3.length);
        System.arraycopy(bArr4, 0, bArr5, length2 + bArr3.length, bArr4.length);
        return bArr5;
    }

    public static byte[] concatenate(byte[][] bArr) {
        int i = 0;
        for (int i2 = 0; i2 != bArr.length; i2++) {
            i += bArr[i2].length;
        }
        byte[] bArr2 = new byte[i];
        int i3 = 0;
        for (int i4 = 0; i4 != bArr.length; i4++) {
            System.arraycopy(bArr[i4], 0, bArr2, i3, bArr[i4].length);
            i3 += bArr[i4].length;
        }
        return bArr2;
    }

    public static int[] concatenate(int[] iArr, int[] iArr2) {
        if (iArr == null) {
            return clone(iArr2);
        }
        if (iArr2 == null) {
            return clone(iArr);
        }
        int[] iArr3 = new int[(iArr.length + iArr2.length)];
        System.arraycopy(iArr, 0, iArr3, 0, iArr.length);
        System.arraycopy(iArr2, 0, iArr3, iArr.length, iArr2.length);
        return iArr3;
    }

    public static boolean constantTimeAreEqual(int i, byte[] bArr, int i2, byte[] bArr2, int i3) {
        if (bArr == null) {
            throw new NullPointerException("'a' cannot be null");
        } else if (bArr2 == null) {
            throw new NullPointerException("'b' cannot be null");
        } else if (i < 0) {
            throw new IllegalArgumentException("'len' cannot be negative");
        } else if (i2 > bArr.length - i) {
            throw new IndexOutOfBoundsException("'aOff' value invalid for specified length");
        } else if (i3 <= bArr2.length - i) {
            int i4 = 0;
            for (int i5 = 0; i5 < i; i5++) {
                i4 |= bArr[i2 + i5] ^ bArr2[i3 + i5];
            }
            return i4 == 0;
        } else {
            throw new IndexOutOfBoundsException("'bOff' value invalid for specified length");
        }
    }

    public static boolean constantTimeAreEqual(byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr2 == null) {
            return false;
        }
        if (bArr == bArr2) {
            return true;
        }
        int length = bArr.length < bArr2.length ? bArr.length : bArr2.length;
        int length2 = bArr.length ^ bArr2.length;
        for (int i = 0; i != length; i++) {
            length2 |= bArr[i] ^ bArr2[i];
        }
        while (length < bArr2.length) {
            length2 |= bArr2[length] ^ (~bArr2[length]);
            length++;
        }
        return length2 == 0;
    }

    public static boolean contains(byte[] bArr, byte b) {
        for (byte b2 : bArr) {
            if (b2 == b) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(char[] cArr, char c) {
        for (char c2 : cArr) {
            if (c2 == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(int[] iArr, int i) {
        for (int i2 : iArr) {
            if (i2 == i) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(long[] jArr, long j) {
        for (long j2 : jArr) {
            if (j2 == j) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(short[] sArr, short s) {
        for (short s2 : sArr) {
            if (s2 == s) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(boolean[] zArr, boolean z) {
        for (boolean z2 : zArr) {
            if (z2 == z) {
                return true;
            }
        }
        return false;
    }

    public static byte[] copyOf(byte[] bArr, int i) {
        byte[] bArr2 = new byte[i];
        System.arraycopy(bArr, 0, bArr2, 0, Math.min(bArr.length, i));
        return bArr2;
    }

    public static char[] copyOf(char[] cArr, int i) {
        char[] cArr2 = new char[i];
        System.arraycopy(cArr, 0, cArr2, 0, Math.min(cArr.length, i));
        return cArr2;
    }

    public static int[] copyOf(int[] iArr, int i) {
        int[] iArr2 = new int[i];
        System.arraycopy(iArr, 0, iArr2, 0, Math.min(iArr.length, i));
        return iArr2;
    }

    public static long[] copyOf(long[] jArr, int i) {
        long[] jArr2 = new long[i];
        System.arraycopy(jArr, 0, jArr2, 0, Math.min(jArr.length, i));
        return jArr2;
    }

    public static BigInteger[] copyOf(BigInteger[] bigIntegerArr, int i) {
        BigInteger[] bigIntegerArr2 = new BigInteger[i];
        System.arraycopy(bigIntegerArr, 0, bigIntegerArr2, 0, Math.min(bigIntegerArr.length, i));
        return bigIntegerArr2;
    }

    public static short[] copyOf(short[] sArr, int i) {
        short[] sArr2 = new short[i];
        System.arraycopy(sArr, 0, sArr2, 0, Math.min(sArr.length, i));
        return sArr2;
    }

    public static boolean[] copyOf(boolean[] zArr, int i) {
        boolean[] zArr2 = new boolean[i];
        System.arraycopy(zArr, 0, zArr2, 0, Math.min(zArr.length, i));
        return zArr2;
    }

    public static byte[] copyOfRange(byte[] bArr, int i, int i2) {
        int length = getLength(i, i2);
        byte[] bArr2 = new byte[length];
        System.arraycopy(bArr, i, bArr2, 0, Math.min(bArr.length - i, length));
        return bArr2;
    }

    public static char[] copyOfRange(char[] cArr, int i, int i2) {
        int length = getLength(i, i2);
        char[] cArr2 = new char[length];
        System.arraycopy(cArr, i, cArr2, 0, Math.min(cArr.length - i, length));
        return cArr2;
    }

    public static int[] copyOfRange(int[] iArr, int i, int i2) {
        int length = getLength(i, i2);
        int[] iArr2 = new int[length];
        System.arraycopy(iArr, i, iArr2, 0, Math.min(iArr.length - i, length));
        return iArr2;
    }

    public static long[] copyOfRange(long[] jArr, int i, int i2) {
        int length = getLength(i, i2);
        long[] jArr2 = new long[length];
        System.arraycopy(jArr, i, jArr2, 0, Math.min(jArr.length - i, length));
        return jArr2;
    }

    public static BigInteger[] copyOfRange(BigInteger[] bigIntegerArr, int i, int i2) {
        int length = getLength(i, i2);
        BigInteger[] bigIntegerArr2 = new BigInteger[length];
        System.arraycopy(bigIntegerArr, i, bigIntegerArr2, 0, Math.min(bigIntegerArr.length - i, length));
        return bigIntegerArr2;
    }

    public static short[] copyOfRange(short[] sArr, int i, int i2) {
        int length = getLength(i, i2);
        short[] sArr2 = new short[length];
        System.arraycopy(sArr, i, sArr2, 0, Math.min(sArr.length - i, length));
        return sArr2;
    }

    public static boolean[] copyOfRange(boolean[] zArr, int i, int i2) {
        int length = getLength(i, i2);
        boolean[] zArr2 = new boolean[length];
        System.arraycopy(zArr, i, zArr2, 0, Math.min(zArr.length - i, length));
        return zArr2;
    }

    public static void fill(byte[] bArr, byte b) {
        java.util.Arrays.fill(bArr, b);
    }

    public static void fill(byte[] bArr, int i, byte b) {
        fill(bArr, i, bArr.length, b);
    }

    public static void fill(byte[] bArr, int i, int i2, byte b) {
        java.util.Arrays.fill(bArr, i, i2, b);
    }

    public static void fill(char[] cArr, char c) {
        java.util.Arrays.fill(cArr, c);
    }

    public static void fill(char[] cArr, int i, int i2, char c) {
        java.util.Arrays.fill(cArr, i, i2, c);
    }

    public static void fill(int[] iArr, int i) {
        java.util.Arrays.fill(iArr, i);
    }

    public static void fill(int[] iArr, int i, int i2) {
        java.util.Arrays.fill(iArr, i, iArr.length, i2);
    }

    public static void fill(int[] iArr, int i, int i2, int i3) {
        java.util.Arrays.fill(iArr, i, i2, i3);
    }

    public static void fill(long[] jArr, int i, int i2, long j) {
        java.util.Arrays.fill(jArr, i, i2, j);
    }

    public static void fill(long[] jArr, int i, long j) {
        java.util.Arrays.fill(jArr, i, jArr.length, j);
    }

    public static void fill(long[] jArr, long j) {
        java.util.Arrays.fill(jArr, j);
    }

    public static void fill(Object[] objArr, int i, int i2, Object obj) {
        java.util.Arrays.fill(objArr, i, i2, obj);
    }

    public static void fill(Object[] objArr, Object obj) {
        java.util.Arrays.fill(objArr, obj);
    }

    public static void fill(short[] sArr, int i, int i2, short s) {
        java.util.Arrays.fill(sArr, i, i2, s);
    }

    public static void fill(short[] sArr, int i, short s) {
        java.util.Arrays.fill(sArr, i, sArr.length, s);
    }

    public static void fill(short[] sArr, short s) {
        java.util.Arrays.fill(sArr, s);
    }

    public static void fill(boolean[] zArr, int i, int i2, boolean z) {
        java.util.Arrays.fill(zArr, i, i2, z);
    }

    public static void fill(boolean[] zArr, boolean z) {
        java.util.Arrays.fill(zArr, z);
    }

    private static int getLength(int i, int i2) {
        int i3 = i2 - i;
        if (i3 >= 0) {
            return i3;
        }
        StringBuffer stringBuffer = new StringBuffer(i);
        stringBuffer.append(" > ");
        stringBuffer.append(i2);
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    public static int hashCode(byte[] bArr) {
        if (bArr == null) {
            return 0;
        }
        int length = bArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ bArr[length];
        }
    }

    public static int hashCode(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            return 0;
        }
        int i3 = i2 + 1;
        while (true) {
            i2--;
            if (i2 < 0) {
                return i3;
            }
            i3 = (i3 * 257) ^ bArr[i + i2];
        }
    }

    public static int hashCode(char[] cArr) {
        if (cArr == null) {
            return 0;
        }
        int length = cArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ cArr[length];
        }
    }

    public static int hashCode(int[] iArr) {
        if (iArr == null) {
            return 0;
        }
        int length = iArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ iArr[length];
        }
    }

    public static int hashCode(int[] iArr, int i, int i2) {
        if (iArr == null) {
            return 0;
        }
        int i3 = i2 + 1;
        while (true) {
            i2--;
            if (i2 < 0) {
                return i3;
            }
            i3 = (i3 * 257) ^ iArr[i + i2];
        }
    }

    public static int hashCode(long[] jArr) {
        if (jArr == null) {
            return 0;
        }
        int length = jArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            long j = jArr[length];
            i = (((i * 257) ^ ((int) j)) * 257) ^ ((int) (j >>> 32));
        }
    }

    public static int hashCode(long[] jArr, int i, int i2) {
        if (jArr == null) {
            return 0;
        }
        int i3 = i2 + 1;
        while (true) {
            i2--;
            if (i2 < 0) {
                return i3;
            }
            long j = jArr[i + i2];
            i3 = (((i3 * 257) ^ ((int) j)) * 257) ^ ((int) (j >>> 32));
        }
    }

    public static int hashCode(Object[] objArr) {
        if (objArr == null) {
            return 0;
        }
        int length = objArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ Objects.hashCode(objArr[length]);
        }
    }

    public static int hashCode(short[] sArr) {
        if (sArr == null) {
            return 0;
        }
        int length = sArr.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ (sArr[length] & 255);
        }
    }

    public static int hashCode(int[][] iArr) {
        int i = 0;
        for (int i2 = 0; i2 != iArr.length; i2++) {
            i = (i * 257) + hashCode(iArr[i2]);
        }
        return i;
    }

    public static int hashCode(short[][] sArr) {
        int i = 0;
        for (int i2 = 0; i2 != sArr.length; i2++) {
            i = (i * 257) + hashCode(sArr[i2]);
        }
        return i;
    }

    public static int hashCode(short[][][] sArr) {
        int i = 0;
        for (int i2 = 0; i2 != sArr.length; i2++) {
            i = (i * 257) + hashCode(sArr[i2]);
        }
        return i;
    }

    public static boolean isNullOrContainsNull(Object[] objArr) {
        if (objArr == null) {
            return true;
        }
        for (Object obj : objArr) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNullOrEmpty(byte[] bArr) {
        return bArr == null || bArr.length < 1;
    }

    public static boolean isNullOrEmpty(Object[] objArr) {
        return objArr == null || objArr.length < 1;
    }

    public static byte[] prepend(byte[] bArr, byte b) {
        if (bArr == null) {
            return new byte[]{b};
        }
        int length = bArr.length;
        byte[] bArr2 = new byte[(length + 1)];
        System.arraycopy(bArr, 0, bArr2, 1, length);
        bArr2[0] = b;
        return bArr2;
    }

    public static int[] prepend(int[] iArr, int i) {
        if (iArr == null) {
            return new int[]{i};
        }
        int length = iArr.length;
        int[] iArr2 = new int[(length + 1)];
        System.arraycopy(iArr, 0, iArr2, 1, length);
        iArr2[0] = i;
        return iArr2;
    }

    public static short[] prepend(short[] sArr, short s) {
        if (sArr == null) {
            return new short[]{s};
        }
        int length = sArr.length;
        short[] sArr2 = new short[(length + 1)];
        System.arraycopy(sArr, 0, sArr2, 1, length);
        sArr2[0] = s;
        return sArr2;
    }

    public static byte[] reverse(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        int i = 0;
        int length = bArr.length;
        byte[] bArr2 = new byte[length];
        while (true) {
            length--;
            if (length < 0) {
                return bArr2;
            }
            bArr2[length] = bArr[i];
            i++;
        }
    }

    public static int[] reverse(int[] iArr) {
        if (iArr == null) {
            return null;
        }
        int i = 0;
        int length = iArr.length;
        int[] iArr2 = new int[length];
        while (true) {
            length--;
            if (length < 0) {
                return iArr2;
            }
            iArr2[length] = iArr[i];
            i++;
        }
    }
}
