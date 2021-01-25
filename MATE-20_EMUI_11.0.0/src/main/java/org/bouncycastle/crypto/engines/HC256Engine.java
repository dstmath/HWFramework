package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class HC256Engine implements StreamCipher {
    private byte[] buf = new byte[4];
    private int cnt = 0;
    private int idx = 0;
    private boolean initialised;
    private byte[] iv;
    private byte[] key;
    private int[] p = new int[1024];
    private int[] q = new int[1024];

    private byte getByte() {
        if (this.idx == 0) {
            int step = step();
            byte[] bArr = this.buf;
            bArr[0] = (byte) (step & GF2Field.MASK);
            int i = step >> 8;
            bArr[1] = (byte) (i & GF2Field.MASK);
            int i2 = i >> 8;
            bArr[2] = (byte) (i2 & GF2Field.MASK);
            bArr[3] = (byte) ((i2 >> 8) & GF2Field.MASK);
        }
        byte[] bArr2 = this.buf;
        int i3 = this.idx;
        byte b = bArr2[i3];
        this.idx = 3 & (i3 + 1);
        return b;
    }

    private void init() {
        byte[] bArr = this.key;
        if (bArr.length != 32 && bArr.length != 16) {
            throw new IllegalArgumentException("The key must be 128/256 bits long");
        } else if (this.iv.length >= 16) {
            byte[] bArr2 = this.key;
            if (bArr2.length != 32) {
                byte[] bArr3 = new byte[32];
                System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
                byte[] bArr4 = this.key;
                System.arraycopy(bArr4, 0, bArr3, 16, bArr4.length);
                this.key = bArr3;
            }
            byte[] bArr5 = this.iv;
            if (bArr5.length < 32) {
                byte[] bArr6 = new byte[32];
                System.arraycopy(bArr5, 0, bArr6, 0, bArr5.length);
                byte[] bArr7 = this.iv;
                System.arraycopy(bArr7, 0, bArr6, bArr7.length, bArr6.length - bArr7.length);
                this.iv = bArr6;
            }
            this.idx = 0;
            this.cnt = 0;
            int[] iArr = new int[2560];
            for (int i = 0; i < 32; i++) {
                int i2 = i >> 2;
                iArr[i2] = iArr[i2] | ((this.key[i] & 255) << ((i & 3) * 8));
            }
            for (int i3 = 0; i3 < 32; i3++) {
                int i4 = (i3 >> 2) + 8;
                iArr[i4] = iArr[i4] | ((this.iv[i3] & 255) << ((i3 & 3) * 8));
            }
            for (int i5 = 16; i5 < 2560; i5++) {
                int i6 = iArr[i5 - 2];
                int i7 = iArr[i5 - 15];
                iArr[i5] = ((i6 >>> 10) ^ (rotateRight(i6, 17) ^ rotateRight(i6, 19))) + iArr[i5 - 7] + ((i7 >>> 3) ^ (rotateRight(i7, 7) ^ rotateRight(i7, 18))) + iArr[i5 - 16] + i5;
            }
            System.arraycopy(iArr, 512, this.p, 0, 1024);
            System.arraycopy(iArr, 1536, this.q, 0, 1024);
            for (int i8 = 0; i8 < 4096; i8++) {
                step();
            }
            this.cnt = 0;
        } else {
            throw new IllegalArgumentException("The IV must be at least 128 bits long");
        }
    }

    private static int rotateRight(int i, int i2) {
        return (i << (-i2)) | (i >>> i2);
    }

    private int step() {
        int i;
        int i2;
        int i3 = this.cnt;
        int i4 = i3 & 1023;
        if (i3 < 1024) {
            int[] iArr = this.p;
            int i5 = iArr[(i4 - 3) & 1023];
            int i6 = iArr[(i4 - 1023) & 1023];
            int i7 = iArr[i4];
            int rotateRight = iArr[(i4 - 10) & 1023] + (rotateRight(i6, 23) ^ rotateRight(i5, 10));
            int[] iArr2 = this.q;
            iArr[i4] = i7 + rotateRight + iArr2[(i5 ^ i6) & 1023];
            int[] iArr3 = this.p;
            int i8 = iArr3[(i4 - 12) & 1023];
            i = iArr2[i8 & GF2Field.MASK] + iArr2[((i8 >> 8) & GF2Field.MASK) + 256] + iArr2[((i8 >> 16) & GF2Field.MASK) + 512] + iArr2[((i8 >> 24) & GF2Field.MASK) + 768];
            i2 = iArr3[i4];
        } else {
            int[] iArr4 = this.q;
            int i9 = iArr4[(i4 - 3) & 1023];
            int i10 = iArr4[(i4 - 1023) & 1023];
            int i11 = iArr4[i4];
            int rotateRight2 = iArr4[(i4 - 10) & 1023] + (rotateRight(i10, 23) ^ rotateRight(i9, 10));
            int[] iArr5 = this.p;
            iArr4[i4] = i11 + rotateRight2 + iArr5[(i9 ^ i10) & 1023];
            int[] iArr6 = this.q;
            int i12 = iArr6[(i4 - 12) & 1023];
            i = iArr5[i12 & GF2Field.MASK] + iArr5[((i12 >> 8) & GF2Field.MASK) + 256] + iArr5[((i12 >> 16) & GF2Field.MASK) + 512] + iArr5[((i12 >> 24) & GF2Field.MASK) + 768];
            i2 = iArr6[i4];
        }
        int i13 = i2 ^ i;
        this.cnt = (this.cnt + 1) & 2047;
        return i13;
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public String getAlgorithmName() {
        return "HC-256";
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        CipherParameters cipherParameters2;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            this.iv = parametersWithIV.getIV();
            cipherParameters2 = parametersWithIV.getParameters();
        } else {
            this.iv = new byte[0];
            cipherParameters2 = cipherParameters;
        }
        if (cipherParameters2 instanceof KeyParameter) {
            this.key = ((KeyParameter) cipherParameters2).getKey();
            init();
            this.initialised = true;
            return;
        }
        throw new IllegalArgumentException("Invalid parameter passed to HC256 init - " + cipherParameters.getClass().getName());
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
        if (!this.initialised) {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        } else if (i + i2 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i3 + i2 <= bArr2.length) {
            for (int i4 = 0; i4 < i2; i4++) {
                bArr2[i3 + i4] = (byte) (bArr[i + i4] ^ getByte());
            }
            return i2;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public void reset() {
        init();
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public byte returnByte(byte b) {
        return (byte) (b ^ getByte());
    }
}
