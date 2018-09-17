package com.android.org.bouncycastle.crypto.paddings;

import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import java.security.SecureRandom;

public class ISO7816d4Padding implements BlockCipherPadding {
    public void init(SecureRandom random) throws IllegalArgumentException {
    }

    public String getPaddingName() {
        return "ISO7816-4";
    }

    public int addPadding(byte[] in, int inOff) {
        int added = in.length - inOff;
        in[inOff] = Byte.MIN_VALUE;
        while (true) {
            inOff++;
            if (inOff >= in.length) {
                return added;
            }
            in[inOff] = (byte) 0;
        }
    }

    public int padCount(byte[] in) throws InvalidCipherTextException {
        int count = in.length - 1;
        while (count > 0 && in[count] == (byte) 0) {
            count--;
        }
        if (in[count] == Byte.MIN_VALUE) {
            return in.length - count;
        }
        throw new InvalidCipherTextException("pad block corrupted");
    }
}
