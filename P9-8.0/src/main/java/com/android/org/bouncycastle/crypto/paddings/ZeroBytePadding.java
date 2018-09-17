package com.android.org.bouncycastle.crypto.paddings;

import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import java.security.SecureRandom;

public class ZeroBytePadding implements BlockCipherPadding {
    public void init(SecureRandom random) throws IllegalArgumentException {
    }

    public String getPaddingName() {
        return "ZeroByte";
    }

    public int addPadding(byte[] in, int inOff) {
        int added = in.length - inOff;
        while (inOff < in.length) {
            in[inOff] = (byte) 0;
            inOff++;
        }
        return added;
    }

    public int padCount(byte[] in) throws InvalidCipherTextException {
        int count = in.length;
        while (count > 0 && in[count - 1] == (byte) 0) {
            count--;
        }
        return in.length - count;
    }
}
