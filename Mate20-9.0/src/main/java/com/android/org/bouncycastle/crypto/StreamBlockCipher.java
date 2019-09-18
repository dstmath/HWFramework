package com.android.org.bouncycastle.crypto;

public abstract class StreamBlockCipher implements BlockCipher, StreamCipher {
    private final BlockCipher cipher;

    /* access modifiers changed from: protected */
    public abstract byte calculateByte(byte b);

    protected StreamBlockCipher(BlockCipher cipher2) {
        this.cipher = cipher2;
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public final byte returnByte(byte in) {
        return calculateByte(in);
    }

    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) throws DataLengthException {
        if (outOff + len > out.length) {
            throw new DataLengthException("output buffer too short");
        } else if (inOff + len <= in.length) {
            int inEnd = inOff + len;
            int outStart = outOff;
            for (int inStart = inOff; inStart < inEnd; inStart++) {
                out[outStart] = calculateByte(in[inStart]);
                outStart++;
            }
            return len;
        } else {
            throw new DataLengthException("input buffer too small");
        }
    }
}
