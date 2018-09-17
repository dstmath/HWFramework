package com.android.org.bouncycastle.crypto.paddings;

import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import java.security.SecureRandom;

public class PKCS7Padding implements BlockCipherPadding {
    public void init(SecureRandom random) throws IllegalArgumentException {
    }

    public String getPaddingName() {
        return "PKCS7";
    }

    public int addPadding(byte[] in, int inOff) {
        byte code = (byte) (in.length - inOff);
        while (inOff < in.length) {
            in[inOff] = code;
            inOff++;
        }
        return code;
    }

    public int padCount(byte[] in) throws InvalidCipherTextException {
        int i;
        int count = in[in.length - 1] & 255;
        byte countAsbyte = (byte) count;
        if (count > in.length) {
            i = 1;
        } else {
            i = 0;
        }
        boolean failed = i | (count == 0 ? 1 : 0);
        for (int i2 = 0; i2 < in.length; i2++) {
            int i3;
            if (in.length - i2 <= count) {
                i3 = 1;
            } else {
                i3 = 0;
            }
            failed |= i3 & (in[i2] != countAsbyte ? 1 : 0);
        }
        if (!failed) {
            return count;
        }
        throw new InvalidCipherTextException("pad block corrupted");
    }
}
