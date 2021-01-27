package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.digests.DSTU7564Digest;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Pack;

public class DSTU7564Mac implements Mac {
    private static final int BITS_IN_BYTE = 8;
    private DSTU7564Digest engine;
    private long inputLength;
    private byte[] invertedKey = null;
    private int macSize;
    private byte[] paddedKey = null;

    public DSTU7564Mac(int i) {
        this.engine = new DSTU7564Digest(i);
        this.macSize = i / 8;
    }

    private void pad() {
        int byteLength = this.engine.getByteLength() - ((int) (this.inputLength % ((long) this.engine.getByteLength())));
        if (byteLength < 13) {
            byteLength += this.engine.getByteLength();
        }
        byte[] bArr = new byte[byteLength];
        bArr[0] = Byte.MIN_VALUE;
        Pack.longToLittleEndian(this.inputLength * 8, bArr, bArr.length - 12);
        this.engine.update(bArr, 0, bArr.length);
    }

    private byte[] padKey(byte[] bArr) {
        int length = (((bArr.length + this.engine.getByteLength()) - 1) / this.engine.getByteLength()) * this.engine.getByteLength();
        if (length - bArr.length < 13) {
            length += this.engine.getByteLength();
        }
        byte[] bArr2 = new byte[length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        bArr2[bArr.length] = Byte.MIN_VALUE;
        Pack.intToLittleEndian(bArr.length * 8, bArr2, bArr2.length - 12);
        return bArr2;
    }

    @Override // org.bouncycastle.crypto.Mac
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        if (this.paddedKey == null) {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        } else if (bArr.length - i >= this.macSize) {
            pad();
            DSTU7564Digest dSTU7564Digest = this.engine;
            byte[] bArr2 = this.invertedKey;
            dSTU7564Digest.update(bArr2, 0, bArr2.length);
            this.inputLength = 0;
            int doFinal = this.engine.doFinal(bArr, i);
            reset();
            return doFinal;
        } else {
            throw new OutputLengthException("Output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public String getAlgorithmName() {
        return "DSTU7564Mac";
    }

    @Override // org.bouncycastle.crypto.Mac
    public int getMacSize() {
        return this.macSize;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        this.paddedKey = null;
        reset();
        if (cipherParameters instanceof KeyParameter) {
            byte[] key = ((KeyParameter) cipherParameters).getKey();
            this.invertedKey = new byte[key.length];
            this.paddedKey = padKey(key);
            int i = 0;
            while (true) {
                byte[] bArr = this.invertedKey;
                if (i < bArr.length) {
                    bArr[i] = (byte) (~key[i]);
                    i++;
                } else {
                    DSTU7564Digest dSTU7564Digest = this.engine;
                    byte[] bArr2 = this.paddedKey;
                    dSTU7564Digest.update(bArr2, 0, bArr2.length);
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("Bad parameter passed");
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public void reset() {
        this.inputLength = 0;
        this.engine.reset();
        byte[] bArr = this.paddedKey;
        if (bArr != null) {
            this.engine.update(bArr, 0, bArr.length);
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte b) throws IllegalStateException {
        this.engine.update(b);
        this.inputLength++;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte[] bArr, int i, int i2) throws DataLengthException, IllegalStateException {
        if (bArr.length - i < i2) {
            throw new DataLengthException("Input buffer too short");
        } else if (this.paddedKey != null) {
            this.engine.update(bArr, i, i2);
            this.inputLength += (long) i2;
        } else {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        }
    }
}
