package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.engines.DSTU7624Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;

public class DSTU7624Mac implements Mac {
    private static final int BITS_IN_BYTE = 8;
    private int blockSize;
    private byte[] buf = new byte[this.blockSize];
    private int bufOff;
    private byte[] c = new byte[this.blockSize];
    private byte[] cTemp = new byte[this.blockSize];
    private DSTU7624Engine engine;
    private byte[] kDelta = new byte[this.blockSize];
    private int macSize;

    public DSTU7624Mac(int i, int i2) {
        this.engine = new DSTU7624Engine(i);
        this.blockSize = i / 8;
        this.macSize = i2 / 8;
    }

    private void processBlock(byte[] bArr, int i) {
        xor(this.c, 0, bArr, i, this.cTemp);
        this.engine.processBlock(this.cTemp, 0, this.c, 0);
    }

    private void xor(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3) {
        if (bArr.length - i < this.blockSize || bArr2.length - i2 < this.blockSize || bArr3.length < this.blockSize) {
            throw new IllegalArgumentException("some of input buffers too short");
        }
        for (int i3 = 0; i3 < this.blockSize; i3++) {
            bArr3[i3] = (byte) (bArr[i3 + i] ^ bArr2[i3 + i2]);
        }
    }

    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        if (this.bufOff % this.buf.length == 0) {
            xor(this.c, 0, this.buf, 0, this.cTemp);
            xor(this.cTemp, 0, this.kDelta, 0, this.c);
            this.engine.processBlock(this.c, 0, this.c, 0);
            if (this.macSize + i <= bArr.length) {
                System.arraycopy(this.c, 0, bArr, i, this.macSize);
                return this.macSize;
            }
            throw new OutputLengthException("output buffer too short");
        }
        throw new DataLengthException("input must be a multiple of blocksize");
    }

    public String getAlgorithmName() {
        return "DSTU7624Mac";
    }

    public int getMacSize() {
        return this.macSize;
    }

    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof KeyParameter) {
            this.engine.init(true, cipherParameters);
            this.engine.processBlock(this.kDelta, 0, this.kDelta, 0);
            return;
        }
        throw new IllegalArgumentException("Invalid parameter passed to DSTU7624Mac");
    }

    public void reset() {
        Arrays.fill(this.c, (byte) 0);
        Arrays.fill(this.cTemp, (byte) 0);
        Arrays.fill(this.kDelta, (byte) 0);
        Arrays.fill(this.buf, (byte) 0);
        this.engine.reset();
        this.engine.processBlock(this.kDelta, 0, this.kDelta, 0);
        this.bufOff = 0;
    }

    public void update(byte b) {
        if (this.bufOff == this.buf.length) {
            processBlock(this.buf, 0);
            this.bufOff = 0;
        }
        byte[] bArr = this.buf;
        int i = this.bufOff;
        this.bufOff = i + 1;
        bArr[i] = b;
    }

    public void update(byte[] bArr, int i, int i2) {
        if (i2 >= 0) {
            int blockSize2 = this.engine.getBlockSize();
            int i3 = blockSize2 - this.bufOff;
            if (i2 > i3) {
                System.arraycopy(bArr, i, this.buf, this.bufOff, i3);
                processBlock(this.buf, 0);
                this.bufOff = 0;
                i2 -= i3;
                i += i3;
                while (i2 > blockSize2) {
                    processBlock(bArr, i);
                    i2 -= blockSize2;
                    i += blockSize2;
                }
            }
            System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
            this.bufOff += i2;
            return;
        }
        throw new IllegalArgumentException("can't have a negative input length!");
    }
}
