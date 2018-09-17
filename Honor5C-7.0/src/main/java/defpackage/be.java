package defpackage;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/* renamed from: be */
final class be {
    private static byte[] a(byte[] bArr, int i, byte[] bArr2, int i2, int i3) {
        if (bArr == null || bArr2 == null) {
            return null;
        }
        int i4;
        if (i <= 0 || i > bArr.length) {
            i = bArr.length;
        }
        int length = (i2 <= 0 || i2 > bArr2.length) ? bArr2.length : i2;
        if (length > 16) {
            length = 16;
        }
        byte[] bArr3 = new byte[16];
        for (i4 = 0; i4 < 16; i4++) {
            bArr3[i4] = (byte) 0;
        }
        for (i4 = 0; i4 < length; i4++) {
            bArr3[i4] = bArr2[i4];
        }
        Cipher instance = Cipher.getInstance("AES/ECB/PKCS5Padding");
        instance.init(i3 == 0 ? 1 : 2, new SecretKeySpec(bArr3, 0, 16, "AES"));
        return instance.doFinal(bArr, 0, i);
    }

    public static byte[] decode(byte[] bArr, int i, byte[] bArr2, int i2) {
        return be.a(bArr, i, bArr2, i2, 1);
    }

    public static byte[] encode(byte[] bArr, int i, byte[] bArr2, int i2) {
        return be.a(bArr, i, bArr2, i2, 0);
    }
}
