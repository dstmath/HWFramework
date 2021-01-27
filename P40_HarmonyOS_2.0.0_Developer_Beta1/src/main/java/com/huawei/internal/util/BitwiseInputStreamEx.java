package com.huawei.internal.util;

import com.android.internal.util.BitwiseInputStream;

public class BitwiseInputStreamEx {
    private BitwiseInputStream mBitwiseInputStream;

    public BitwiseInputStreamEx() {
    }

    public BitwiseInputStreamEx(byte[] buf) {
        this.mBitwiseInputStream = new BitwiseInputStream(buf);
    }

    public int available() {
        BitwiseInputStream bitwiseInputStream = this.mBitwiseInputStream;
        if (bitwiseInputStream != null) {
            return bitwiseInputStream.available();
        }
        return 0;
    }

    public int read(int bits) throws AccessExceptionEx {
        BitwiseInputStream bitwiseInputStream = this.mBitwiseInputStream;
        if (bitwiseInputStream != null) {
            try {
                return bitwiseInputStream.read(bits);
            } catch (BitwiseInputStream.AccessException e) {
                throw new AccessExceptionEx("illegal read");
            }
        } else {
            throw new AccessExceptionEx("null stream");
        }
    }

    public static class AccessExceptionEx extends Exception {
        public AccessExceptionEx(String s) {
            super("BitwiseInputStream access failed: " + s);
        }
    }
}
