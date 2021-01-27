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
    private byte[] buf;
    private int bufOff;
    private byte[] c;
    private byte[] cTemp;
    private DSTU7624Engine engine;
    private boolean initCalled = false;
    private byte[] kDelta;
    private int macSize;

    public DSTU7624Mac(int i, int i2) {
        this.engine = new DSTU7624Engine(i);
        this.blockSize = i / 8;
        this.macSize = i2 / 8;
        int i3 = this.blockSize;
        this.c = new byte[i3];
        this.kDelta = new byte[i3];
        this.cTemp = new byte[i3];
        this.buf = new byte[i3];
    }

    private void processBlock(byte[] bArr, int i) {
        xor(this.c, 0, bArr, i, this.cTemp);
        this.engine.processBlock(this.cTemp, 0, this.c, 0);
    }

    private void xor(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3) {
        int length = bArr.length - i;
        int i3 = this.blockSize;
        if (length < i3 || bArr2.length - i2 < i3 || bArr3.length < i3) {
            throw new IllegalArgumentException("some of input buffers too short");
        }
        for (int i4 = 0; i4 < this.blockSize; i4++) {
            bArr3[i4] = (byte) (bArr[i4 + i] ^ bArr2[i4 + i2]);
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        int i2 = this.bufOff;
        byte[] bArr2 = this.buf;
        if (i2 % bArr2.length == 0) {
            xor(this.c, 0, bArr2, 0, this.cTemp);
            xor(this.cTemp, 0, this.kDelta, 0, this.c);
            DSTU7624Engine dSTU7624Engine = this.engine;
            byte[] bArr3 = this.c;
            dSTU7624Engine.processBlock(bArr3, 0, bArr3, 0);
            int i3 = this.macSize;
            if (i3 + i <= bArr.length) {
                System.arraycopy(this.c, 0, bArr, i, i3);
                reset();
                return this.macSize;
            }
            throw new OutputLengthException("output buffer too short");
        }
        throw new DataLengthException("input must be a multiple of blocksize");
    }

    @Override // org.bouncycastle.crypto.Mac
    public String getAlgorithmName() {
        return "DSTU7624Mac";
    }

    @Override // org.bouncycastle.crypto.Mac
    public int getMacSize() {
        return this.macSize;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof KeyParameter) {
            this.engine.init(true, cipherParameters);
            this.initCalled = true;
            reset();
            return;
        }
        throw new IllegalArgumentException("Invalid parameter passed to DSTU7624Mac");
    }

    @Override // org.bouncycastle.crypto.Mac
    public void reset() {
        Arrays.fill(this.c, (byte) 0);
        Arrays.fill(this.cTemp, (byte) 0);
        Arrays.fill(this.kDelta, (byte) 0);
        Arrays.fill(this.buf, (byte) 0);
        this.engine.reset();
        if (this.initCalled) {
            DSTU7624Engine dSTU7624Engine = this.engine;
            byte[] bArr = this.kDelta;
            dSTU7624Engine.processBlock(bArr, 0, bArr, 0);
        }
        this.bufOff = 0;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte b) {
        int i = this.bufOff;
        byte[] bArr = this.buf;
        if (i == bArr.length) {
            processBlock(bArr, 0);
            this.bufOff = 0;
        }
        byte[] bArr2 = this.buf;
        int i2 = this.bufOff;
        this.bufOff = i2 + 1;
        bArr2[i2] = b;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte[] bArr, int i, int i2) {
        if (i2 >= 0) {
            int blockSize2 = this.engine.getBlockSize();
            int i3 = this.bufOff;
            int i4 = blockSize2 - i3;
            if (i2 > i4) {
                System.arraycopy(bArr, i, this.buf, i3, i4);
                processBlock(this.buf, 0);
                this.bufOff = 0;
                i2 -= i4;
                i += i4;
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
