package org.bouncycastle.crypto.prng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class FixedSecureRandom extends SecureRandom {
    private byte[] _data;
    private int _index;
    private int _intPad;

    public FixedSecureRandom(boolean z, byte[] bArr) {
        this(z, new byte[][]{bArr});
    }

    public FixedSecureRandom(boolean z, byte[][] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        while (i != bArr.length) {
            try {
                byteArrayOutputStream.write(bArr[i]);
                i++;
            } catch (IOException e) {
                throw new IllegalArgumentException("can't save value array.");
            }
        }
        this._data = byteArrayOutputStream.toByteArray();
        if (z) {
            this._intPad = this._data.length % 4;
        }
    }

    public FixedSecureRandom(byte[] bArr) {
        this(false, new byte[][]{bArr});
    }

    public FixedSecureRandom(byte[][] bArr) {
        this(false, bArr);
    }

    private int nextValue() {
        byte[] bArr = this._data;
        int i = this._index;
        this._index = i + 1;
        return bArr[i] & 255;
    }

    public byte[] generateSeed(int i) {
        byte[] bArr = new byte[i];
        nextBytes(bArr);
        return bArr;
    }

    public boolean isExhausted() {
        return this._index == this._data.length;
    }

    public void nextBytes(byte[] bArr) {
        System.arraycopy(this._data, this._index, bArr, 0, bArr.length);
        this._index += bArr.length;
    }

    public int nextInt() {
        int nextValue = (nextValue() << 24) | 0 | (nextValue() << 16);
        if (this._intPad == 2) {
            this._intPad--;
        } else {
            nextValue |= nextValue() << 8;
        }
        if (this._intPad != 1) {
            return nextValue | nextValue();
        }
        this._intPad--;
        return nextValue;
    }

    public long nextLong() {
        return (((long) nextValue()) << 56) | 0 | (((long) nextValue()) << 48) | (((long) nextValue()) << 40) | (((long) nextValue()) << 32) | (((long) nextValue()) << 24) | (((long) nextValue()) << 16) | (((long) nextValue()) << 8) | ((long) nextValue());
    }
}
