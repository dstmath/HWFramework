package org.bouncycastle.crypto.paddings;

import java.security.SecureRandom;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class X923Padding implements BlockCipherPadding {
    SecureRandom random = null;

    public int addPadding(byte[] bArr, int i) {
        byte length = (byte) (bArr.length - i);
        while (i < bArr.length - 1) {
            if (this.random == null) {
                bArr[i] = 0;
            } else {
                bArr[i] = (byte) this.random.nextInt();
            }
            i++;
        }
        bArr[i] = length;
        return length;
    }

    public String getPaddingName() {
        return "X9.23";
    }

    public void init(SecureRandom secureRandom) throws IllegalArgumentException {
        this.random = secureRandom;
    }

    public int padCount(byte[] bArr) throws InvalidCipherTextException {
        byte b = bArr[bArr.length - 1] & 255;
        if (b <= bArr.length) {
            return b;
        }
        throw new InvalidCipherTextException("pad block corrupted");
    }
}
