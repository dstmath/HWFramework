package com.android.org.bouncycastle.crypto;

public abstract class StreamBlockCipher implements BlockCipher, StreamCipher {
    private final BlockCipher cipher;

    protected abstract byte calculateByte(byte b);

    protected StreamBlockCipher(BlockCipher cipher) {
        this.cipher = cipher;
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
        } else if (inOff + len > in.length) {
            throw new DataLengthException("input buffer too small");
        } else {
            int inEnd = inOff + len;
            int outStart = outOff;
            int inStart = inOff;
            while (inStart < inEnd) {
                int outStart2 = outStart + 1;
                int inStart2 = inStart + 1;
                out[outStart] = calculateByte(in[inStart]);
                outStart = outStart2;
                inStart = inStart2;
            }
            return len;
        }
    }
}
