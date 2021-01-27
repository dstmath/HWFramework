package com.huawei.internal.util;

import com.android.internal.util.BitwiseOutputStream;

public class BitwiseOutputStreamEx {
    private BitwiseOutputStream mBitwiseOutputStream;

    public BitwiseOutputStreamEx() {
    }

    public BitwiseOutputStreamEx(int startingLength) {
        this.mBitwiseOutputStream = new BitwiseOutputStream(startingLength);
    }

    public BitwiseOutputStream getBitwiseOutputStream() {
        return this.mBitwiseOutputStream;
    }

    public void setBitwiseOutputStream(BitwiseOutputStream bitwiseOutputStream) {
        this.mBitwiseOutputStream = bitwiseOutputStream;
    }

    public void write(int bits, int data) throws AccessExceptionEx {
        BitwiseOutputStream bitwiseOutputStream = this.mBitwiseOutputStream;
        if (bitwiseOutputStream != null) {
            try {
                bitwiseOutputStream.write(bits, data);
            } catch (BitwiseOutputStream.AccessException e) {
                throw new AccessExceptionEx("illegal write");
            }
        }
    }

    public byte[] toByteArray() {
        return this.mBitwiseOutputStream.toByteArray();
    }

    public static class AccessExceptionEx extends Exception {
        public AccessExceptionEx(String s) {
            super("BitwiseOutputStream access failed: " + s);
        }
    }
}
