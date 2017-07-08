package com.android.org.bouncycastle.crypto.paddings;

import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import java.security.SecureRandom;

public class ISO10126d2Padding implements BlockCipherPadding {
    SecureRandom random;

    public void init(SecureRandom random) throws IllegalArgumentException {
        if (random != null) {
            this.random = random;
        } else {
            this.random = new SecureRandom();
        }
    }

    public String getPaddingName() {
        return "ISO10126-2";
    }

    public int addPadding(byte[] in, int inOff) {
        byte code = (byte) (in.length - inOff);
        while (inOff < in.length - 1) {
            in[inOff] = (byte) this.random.nextInt();
            inOff++;
        }
        in[inOff] = code;
        return code;
    }

    public int padCount(byte[] in) throws InvalidCipherTextException {
        int count = in[in.length - 1] & 255;
        if (count <= in.length) {
            return count;
        }
        throw new InvalidCipherTextException("pad block corrupted");
    }
}
