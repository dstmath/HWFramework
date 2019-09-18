package org.bouncycastle.crypto.paddings;

import java.security.SecureRandom;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class ISO10126d2Padding implements BlockCipherPadding {
    SecureRandom random;

    public int addPadding(byte[] bArr, int i) {
        byte length = (byte) (bArr.length - i);
        while (i < bArr.length - 1) {
            bArr[i] = (byte) this.random.nextInt();
            i++;
        }
        bArr[i] = length;
        return length;
    }

    public String getPaddingName() {
        return "ISO10126-2";
    }

    public void init(SecureRandom secureRandom) throws IllegalArgumentException {
        if (secureRandom == null) {
            secureRandom = CryptoServicesRegistrar.getSecureRandom();
        }
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
