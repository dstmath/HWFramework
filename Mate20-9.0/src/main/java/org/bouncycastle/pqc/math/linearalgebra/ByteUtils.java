package org.bouncycastle.pqc.math.linearalgebra;

public final class ByteUtils {
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private ByteUtils() {
    }

    public static byte[] clone(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        byte[] bArr2 = new byte[bArr.length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    public static byte[] concatenate(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[(bArr.length + bArr2.length)];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return bArr3;
    }

    public static byte[] concatenate(byte[][] bArr) {
        int length = bArr[0].length;
        byte[] bArr2 = new byte[(bArr.length * length)];
        int i = 0;
        for (byte[] arraycopy : bArr) {
            System.arraycopy(arraycopy, 0, bArr2, i, length);
            i += length;
        }
        return bArr2;
    }

    public static int deepHashCode(byte[] bArr) {
        int i = 1;
        for (byte b : bArr) {
            i = b + (31 * i);
        }
        return i;
    }

    public static int deepHashCode(byte[][] bArr) {
        int i = 1;
        for (byte[] deepHashCode : bArr) {
            i = deepHashCode(deepHashCode) + (31 * i);
        }
        return i;
    }

    public static int deepHashCode(byte[][][] bArr) {
        int i = 1;
        for (byte[][] deepHashCode : bArr) {
            i = deepHashCode(deepHashCode) + (31 * i);
        }
        return i;
    }

    public static boolean equals(byte[] bArr, byte[] bArr2) {
        boolean z = false;
        if (bArr == null) {
            if (bArr2 == null) {
                z = true;
            }
            return z;
        } else if (bArr2 == null || bArr.length != bArr2.length) {
            return false;
        } else {
            boolean z2 = true;
            for (int length = bArr.length - 1; length >= 0; length--) {
                z2 &= bArr[length] == bArr2[length];
            }
            return z2;
        }
    }

    public static boolean equals(byte[][] bArr, byte[][] bArr2) {
        if (bArr.length != bArr2.length) {
            return false;
        }
        boolean z = true;
        for (int length = bArr.length - 1; length >= 0; length--) {
            z &= equals(bArr[length], bArr2[length]);
        }
        return z;
    }

    public static boolean equals(byte[][][] bArr, byte[][][] bArr2) {
        if (bArr.length != bArr2.length) {
            return false;
        }
        boolean z = true;
        for (int length = bArr.length - 1; length >= 0; length--) {
            if (bArr[length].length != bArr2[length].length) {
                return false;
            }
            for (int length2 = bArr[length].length - 1; length2 >= 0; length2--) {
                z &= equals(bArr[length][length2], bArr2[length][length2]);
            }
        }
        return z;
    }

    public static byte[] fromHexString(String str) {
        char[] charArray = str.toUpperCase().toCharArray();
        int i = 0;
        for (int i2 = 0; i2 < charArray.length; i2++) {
            if ((charArray[i2] >= '0' && charArray[i2] <= '9') || (charArray[i2] >= 'A' && charArray[i2] <= 'F')) {
                i++;
            }
        }
        byte[] bArr = new byte[((i + 1) >> 1)];
        int i3 = i & 1;
        for (int i4 = 0; i4 < charArray.length; i4++) {
            if (charArray[i4] < '0' || charArray[i4] > '9') {
                if (charArray[i4] >= 'A' && charArray[i4] <= 'F') {
                    int i5 = i3 >> 1;
                    bArr[i5] = (byte) (bArr[i5] << 4);
                    bArr[i5] = (byte) (bArr[i5] | ((charArray[i4] - 'A') + 10));
                }
            } else {
                int i6 = i3 >> 1;
                bArr[i6] = (byte) (bArr[i6] << 4);
                bArr[i6] = (byte) (bArr[i6] | (charArray[i4] - '0'));
            }
            i3++;
        }
        return bArr;
    }

    public static byte[][] split(byte[] bArr, int i) throws ArrayIndexOutOfBoundsException {
        if (i <= bArr.length) {
            byte[][] bArr2 = {new byte[i], new byte[(bArr.length - i)]};
            System.arraycopy(bArr, 0, bArr2[0], 0, i);
            System.arraycopy(bArr, i, bArr2[1], 0, bArr.length - i);
            return bArr2;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public static byte[] subArray(byte[] bArr, int i) {
        return subArray(bArr, i, bArr.length);
    }

    public static byte[] subArray(byte[] bArr, int i, int i2) {
        int i3 = i2 - i;
        byte[] bArr2 = new byte[i3];
        System.arraycopy(bArr, i, bArr2, 0, i3);
        return bArr2;
    }

    public static String toBinaryString(byte[] bArr) {
        String str = "";
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i];
            String str2 = str;
            for (int i2 = 0; i2 < 8; i2++) {
                str2 = str2 + ((b >>> i2) & 1);
            }
            if (i != bArr.length - 1) {
                str = str2 + " ";
            } else {
                str = str2;
            }
        }
        return str;
    }

    public static char[] toCharArray(byte[] bArr) {
        char[] cArr = new char[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            cArr[i] = (char) bArr[i];
        }
        return cArr;
    }

    public static String toHexString(byte[] bArr) {
        String str = "";
        for (int i = 0; i < bArr.length; i++) {
            String str2 = str + HEX_CHARS[(bArr[i] >>> 4) & 15];
            str = str2 + HEX_CHARS[bArr[i] & 15];
        }
        return str;
    }

    public static String toHexString(byte[] bArr, String str, String str2) {
        String str3 = new String(str);
        for (int i = 0; i < bArr.length; i++) {
            String str4 = str3 + HEX_CHARS[(bArr[i] >>> 4) & 15];
            str3 = str4 + HEX_CHARS[bArr[i] & 15];
            if (i < bArr.length - 1) {
                str3 = str3 + str2;
            }
        }
        return str3;
    }

    public static byte[] xor(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[bArr.length];
        for (int length = bArr.length - 1; length >= 0; length--) {
            bArr3[length] = (byte) (bArr[length] ^ bArr2[length]);
        }
        return bArr3;
    }
}
