package org.bouncycastle.crypto.paddings;

import java.security.SecureRandom;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class TBCPadding implements BlockCipherPadding {
    public int addPadding(byte[] bArr, int i) {
        int length = bArr.length - i;
        int i2 = 0;
        if (i <= 0 ? (bArr[bArr.length - 1] & 1) == 0 : (bArr[i - 1] & 1) == 0) {
            i2 = 255;
        }
        byte b = (byte) i2;
        while (i < bArr.length) {
            bArr[i] = b;
            i++;
        }
        return length;
    }

    public String getPaddingName() {
        return "TBC";
    }

    public void init(SecureRandom secureRandom) throws IllegalArgumentException {
    }

    public int padCount(byte[] bArr) throws InvalidCipherTextException {
        byte b = bArr[bArr.length - 1];
        int length = bArr.length - 1;
        while (length > 0 && bArr[length - 1] == b) {
            length--;
        }
        return bArr.length - length;
    }
}
