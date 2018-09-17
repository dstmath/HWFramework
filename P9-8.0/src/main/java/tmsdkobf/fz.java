package tmsdkobf;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class fz {
    public static byte[] P() {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            String str = "http://pmir.3g.qq.com";
            int[] iArr = new int[]{-36, -46, -45, -77, -22, -10, 47, -77, -72, -69, -32, 25, 21, -21, -6, -75, -71, 31, -39, -49, -49};
            for (int i = 0; i < str.length(); i++) {
                stringBuffer.append((char) (str.charAt(i) + iArr[i]));
            }
            return stringBuffer.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void a(byte[] bArr, int[] iArr) {
        int i;
        int length = bArr.length >> 2;
        int i2 = 0;
        int i3 = 0;
        while (i2 < length) {
            i = i3 + 1;
            iArr[i2] = bArr[i3] & 255;
            i3 = i + 1;
            iArr[i2] = iArr[i2] | ((bArr[i] & 255) << 8);
            i = i3 + 1;
            iArr[i2] = iArr[i2] | ((bArr[i3] & 255) << 16);
            i3 = i + 1;
            iArr[i2] = iArr[i2] | ((bArr[i] & 255) << 24);
            i2++;
        }
        if (i3 >= bArr.length) {
            i = i3;
            return;
        }
        i = i3 + 1;
        iArr[i2] = bArr[i3] & 255;
        int i4 = 8;
        while (i < bArr.length) {
            iArr[i2] = iArr[i2] | ((bArr[i] & 255) << i4);
            i++;
            i4 += 8;
        }
    }

    private static void a(int[] iArr, int -l_5_I, byte[] bArr) {
        int i;
        int length = bArr.length >> 2;
        if (length > -l_5_I) {
            length = -l_5_I;
        }
        int i2 = 0;
        int i3 = 0;
        while (i2 < length) {
            i = i3 + 1;
            bArr[i3] = (byte) ((byte) (iArr[i2] & 255));
            i3 = i + 1;
            bArr[i] = (byte) ((byte) ((iArr[i2] >>> 8) & 255));
            i = i3 + 1;
            bArr[i3] = (byte) ((byte) ((iArr[i2] >>> 16) & 255));
            i3 = i + 1;
            bArr[i] = (byte) ((byte) ((iArr[i2] >>> 24) & 255));
            i2++;
        }
        if (-l_5_I > length && i3 < bArr.length) {
            i = i3 + 1;
            bArr[i3] = (byte) ((byte) (iArr[i2] & 255));
            int i4 = 8;
            while (true) {
                i3 = i;
                if (i4 > 24 || i3 >= bArr.length) {
                    break;
                }
                i = i3 + 1;
                bArr[i3] = (byte) ((byte) ((iArr[i2] >>> i4) & 255));
                i4 += 8;
            }
        }
        i = i3;
    }

    public static byte[] a(byte[] bArr, byte[] bArr2) {
        byte[] d = d(bArr2);
        if (bArr == null || d == null || bArr.length == 0) {
            return bArr;
        }
        int i;
        int length = bArr.length % 4 != 0 ? (bArr.length >>> 2) + 2 : (bArr.length >>> 2) + 1;
        int[] iArr = new int[length];
        a(bArr, iArr);
        iArr[length - 1] = bArr.length;
        length = d.length % 4 != 0 ? (d.length >>> 2) + 1 : d.length >>> 2;
        if (length < 4) {
            length = 4;
        }
        int[] iArr2 = new int[length];
        for (i = 0; i < length; i++) {
            iArr2[i] = 0;
        }
        a(d, iArr2);
        length = iArr.length - 1;
        i = iArr[length];
        int i2 = iArr[0];
        int i3 = 0;
        int i4 = (52 / (length + 1)) + 6;
        while (true) {
            int i5 = i4;
            i4 = i5 - 1;
            if (i5 <= 0) {
                byte[] bArr3 = new byte[(iArr.length << 2)];
                a(iArr, iArr.length, bArr3);
                return bArr3;
            }
            i3 -= 1640531527;
            int i6 = (i3 >>> 2) & 3;
            int i7 = 0;
            while (i7 < length) {
                i2 = iArr[i7 + 1];
                i = iArr[i7] + ((((i >>> 5) ^ (i2 << 2)) + ((i2 >>> 3) ^ (i << 4))) ^ ((i3 ^ i2) + (iArr2[(i7 & 3) ^ i6] ^ i)));
                iArr[i7] = i;
                i7++;
            }
            i2 = iArr[0];
            i = iArr[length] + ((((i >>> 5) ^ (i2 << 2)) + ((i2 >>> 3) ^ (i << 4))) ^ ((i3 ^ i2) + (iArr2[(i7 & 3) ^ i6] ^ i)));
            iArr[length] = i;
        }
    }

    public static byte[] b(byte[] bArr, byte[] bArr2) {
        byte[] d = d(bArr2);
        if (bArr == null || d == null || bArr.length == 0) {
            return bArr;
        }
        if (bArr.length % 4 != 0 || bArr.length < 8) {
            return null;
        }
        int i;
        int[] iArr = new int[(bArr.length >>> 2)];
        a(bArr, iArr);
        int length = d.length % 4 != 0 ? (d.length >>> 2) + 1 : d.length >>> 2;
        if (length < 4) {
            length = 4;
        }
        int[] iArr2 = new int[length];
        for (i = 0; i < length; i++) {
            iArr2[i] = 0;
        }
        a(d, iArr2);
        length = iArr.length - 1;
        i = iArr[length];
        int i2 = iArr[0];
        for (int i3 = ((52 / (length + 1)) + 6) * -1640531527; i3 != 0; i3 -= -1640531527) {
            int i4 = (i3 >>> 2) & 3;
            int i5 = length;
            while (i5 > 0) {
                i = iArr[i5 - 1];
                i2 = iArr[i5] - ((((i >>> 5) ^ (i2 << 2)) + ((i2 >>> 3) ^ (i << 4))) ^ ((i3 ^ i2) + (iArr2[(i5 & 3) ^ i4] ^ i)));
                iArr[i5] = i2;
                i5--;
            }
            i = iArr[length];
            i2 = iArr[0] - ((((i >>> 5) ^ (i2 << 2)) + ((i2 >>> 3) ^ (i << 4))) ^ ((i3 ^ i2) + (iArr2[(i5 & 3) ^ i4] ^ i)));
            iArr[0] = i2;
        }
        length = iArr[iArr.length - 1];
        if (length < 0 || length > ((iArr.length - 1) << 2)) {
            return null;
        }
        byte[] bArr3 = new byte[length];
        a(iArr, iArr.length - 1, bArr3);
        return bArr3;
    }

    private static byte[] d(byte[] -l_1_R) {
        if (-l_1_R == null || -l_1_R.length <= 16) {
            return -l_1_R;
        }
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(-l_1_R);
            return instance.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
