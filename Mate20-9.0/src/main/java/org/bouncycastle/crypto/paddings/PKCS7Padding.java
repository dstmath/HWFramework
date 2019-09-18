package org.bouncycastle.crypto.paddings;

import java.security.SecureRandom;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class PKCS7Padding implements BlockCipherPadding {
    public int addPadding(byte[] bArr, int i) {
        byte length = (byte) (bArr.length - i);
        while (i < bArr.length) {
            bArr[i] = length;
            i++;
        }
        return length;
    }

    public String getPaddingName() {
        return "PKCS7";
    }

    public void init(SecureRandom secureRandom) throws IllegalArgumentException {
    }

    public int padCount(byte[] bArr) throws InvalidCipherTextException {
        byte b = bArr[bArr.length - 1] & 255;
        byte b2 = (byte) b;
        boolean z = (b > bArr.length) | (b == 0);
        for (int i = 0; i < bArr.length; i++) {
            z |= (bArr.length - i <= b) & (bArr[i] != b2);
        }
        if (!z) {
            return b;
        }
        throw new InvalidCipherTextException("pad block corrupted");
    }
}
