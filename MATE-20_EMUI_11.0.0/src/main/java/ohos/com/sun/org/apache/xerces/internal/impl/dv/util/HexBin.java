package ohos.com.sun.org.apache.xerces.internal.impl.dv.util;

public final class HexBin {
    private static final int BASELENGTH = 128;
    private static final int LOOKUPLENGTH = 16;
    private static final byte[] hexNumberTable = new byte[128];
    private static final char[] lookUpHexAlphabet = new char[16];

    static {
        for (int i = 0; i < 128; i++) {
            hexNumberTable[i] = -1;
        }
        for (int i2 = 57; i2 >= 48; i2--) {
            hexNumberTable[i2] = (byte) (i2 - 48);
        }
        for (int i3 = 70; i3 >= 65; i3--) {
            hexNumberTable[i3] = (byte) ((i3 - 65) + 10);
        }
        for (int i4 = 102; i4 >= 97; i4--) {
            hexNumberTable[i4] = (byte) ((i4 - 97) + 10);
        }
        for (int i5 = 0; i5 < 10; i5++) {
            lookUpHexAlphabet[i5] = (char) (i5 + 48);
        }
        for (int i6 = 10; i6 <= 15; i6++) {
            lookUpHexAlphabet[i6] = (char) ((i6 + 65) - 10);
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:12:0x0012 */
    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: byte[] */
    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: byte */
    /* JADX DEBUG: Multi-variable search result rejected for r3v4, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v1 */
    public static String encode(byte[] bArr) {
        if (bArr == 0) {
            return null;
        }
        int length = bArr.length;
        char[] cArr = new char[(length * 2)];
        for (int i = 0; i < length; i++) {
            byte b = bArr[i];
            if (b < 0) {
                b += 256;
            }
            int i2 = i * 2;
            char[] cArr2 = lookUpHexAlphabet;
            cArr[i2] = cArr2[(b == true ? 1 : 0) >> 4];
            cArr[i2 + 1] = cArr2[(b == true ? 1 : 0) & 15];
        }
        return new String(cArr);
    }

    public static byte[] decode(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length % 2 != 0) {
            return null;
        }
        char[] charArray = str.toCharArray();
        int i = length / 2;
        byte[] bArr = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = i2 * 2;
            char c = charArray[i3];
            byte b = c < 128 ? hexNumberTable[c] : -1;
            if (b == -1) {
                return null;
            }
            char c2 = charArray[i3 + 1];
            byte b2 = c2 < 128 ? hexNumberTable[c2] : -1;
            if (b2 == -1) {
                return null;
            }
            bArr[i2] = (byte) (b2 | (b << 4));
        }
        return bArr;
    }
}
