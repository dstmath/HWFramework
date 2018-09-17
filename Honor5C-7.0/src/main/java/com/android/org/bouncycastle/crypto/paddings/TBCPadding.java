package com.android.org.bouncycastle.crypto.paddings;

import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import java.security.SecureRandom;

public class TBCPadding implements BlockCipherPadding {
    public void init(SecureRandom random) throws IllegalArgumentException {
    }

    public String getPaddingName() {
        return "TBC";
    }

    public int addPadding(byte[] in, int inOff) {
        byte code;
        int i = 255;
        int count = in.length - inOff;
        if (inOff > 0) {
            if ((in[inOff - 1] & 1) != 0) {
                i = 0;
            }
            code = (byte) i;
        } else {
            if ((in[in.length - 1] & 1) != 0) {
                i = 0;
            }
            code = (byte) i;
        }
        while (inOff < in.length) {
            in[inOff] = code;
            inOff++;
        }
        return count;
    }

    public int padCount(byte[] in) throws InvalidCipherTextException {
        byte code = in[in.length - 1];
        int index = in.length - 1;
        while (index > 0 && in[index - 1] == code) {
            index--;
        }
        return in.length - index;
    }
}
