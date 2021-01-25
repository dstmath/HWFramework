package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.math.ec.Tnaf;

public class Grain128Engine implements StreamCipher {
    private static final int STATE_SIZE = 4;
    private int index = 4;
    private boolean initialised = false;
    private int[] lfsr;
    private int[] nfsr;
    private byte[] out;
    private int output;
    private byte[] workingIV;
    private byte[] workingKey;

    private byte getKeyStream() {
        if (this.index > 3) {
            oneRound();
            this.index = 0;
        }
        byte[] bArr = this.out;
        int i = this.index;
        this.index = i + 1;
        return bArr[i];
    }

    private int getOutput() {
        int[] iArr = this.nfsr;
        int i = (iArr[0] >>> 2) | (iArr[1] << 30);
        int i2 = (iArr[0] >>> 12) | (iArr[1] << 20);
        int i3 = (iArr[0] >>> 15) | (iArr[1] << 17);
        int i4 = (iArr[1] >>> 4) | (iArr[2] << 28);
        int i5 = (iArr[1] >>> 13) | (iArr[2] << 19);
        int i6 = iArr[2];
        int i7 = (iArr[2] >>> 9) | (iArr[3] << 23);
        int i8 = (iArr[2] >>> 25) | (iArr[3] << 7);
        int i9 = (iArr[3] << 1) | (iArr[2] >>> 31);
        int[] iArr2 = this.lfsr;
        int i10 = (iArr2[0] >>> 8) | (iArr2[1] << 24);
        int i11 = (iArr2[0] >>> 13) | (iArr2[1] << 19);
        int i12 = (iArr2[0] >>> 20) | (iArr2[1] << 12);
        int i13 = (iArr2[1] >>> 10) | (iArr2[2] << 22);
        int i14 = (iArr2[1] >>> 28) | (iArr2[2] << 4);
        int i15 = (iArr2[2] >>> 15) | (iArr2[3] << 17);
        int i16 = ((iArr2[3] << 1) | (iArr2[2] >>> 31)) & i9 & i2;
        return ((((((((i16 ^ ((((i12 & i11) ^ (i2 & i10)) ^ (i9 & i13)) ^ (i14 & i15))) ^ ((iArr2[2] >>> 29) | (iArr2[3] << 3))) ^ i) ^ i3) ^ i4) ^ i5) ^ i6) ^ i7) ^ i8;
    }

    private int getOutputLFSR() {
        int[] iArr = this.lfsr;
        int i = iArr[0];
        int i2 = (iArr[0] >>> 7) | (iArr[1] << 25);
        int i3 = (iArr[1] >>> 6) | (iArr[2] << 26);
        return iArr[3] ^ ((((i2 ^ i) ^ i3) ^ ((iArr[2] >>> 6) | (iArr[3] << 26))) ^ ((iArr[2] >>> 17) | (iArr[3] << 15)));
    }

    private int getOutputNFSR() {
        int[] iArr = this.nfsr;
        int i = iArr[0];
        int i2 = (iArr[0] >>> 3) | (iArr[1] << 29);
        int i3 = (iArr[0] >>> 11) | (iArr[1] << 21);
        int i4 = (iArr[0] >>> 13) | (iArr[1] << 19);
        int i5 = (iArr[0] >>> 17) | (iArr[1] << 15);
        int i6 = (iArr[0] >>> 18) | (iArr[1] << 14);
        int i7 = (iArr[0] >>> 26) | (iArr[1] << 6);
        int i8 = (iArr[0] >>> 27) | (iArr[1] << 5);
        int i9 = (iArr[1] >>> 8) | (iArr[2] << 24);
        int i10 = (iArr[1] >>> 16) | (iArr[2] << 16);
        int i11 = (iArr[1] >>> 24) | (iArr[2] << 8);
        int i12 = (iArr[1] >>> 27) | (iArr[2] << 5);
        int i13 = (iArr[1] >>> 29) | (iArr[2] << 3);
        int i14 = (iArr[2] >>> 1) | (iArr[3] << 31);
        return (((((((iArr[3] ^ (((i ^ i7) ^ i11) ^ ((iArr[2] >>> 27) | (iArr[3] << 5)))) ^ (i2 & ((iArr[2] >>> 3) | (iArr[3] << 29)))) ^ (i3 & i4)) ^ (i5 & i6)) ^ (i8 & i12)) ^ (i9 & i10)) ^ (i13 & i14)) ^ (((iArr[2] >>> 4) | (iArr[3] << 28)) & ((iArr[2] >>> 20) | (iArr[3] << 12)));
    }

    private void initGrain() {
        for (int i = 0; i < 8; i++) {
            this.output = getOutput();
            this.nfsr = shift(this.nfsr, (getOutputNFSR() ^ this.lfsr[0]) ^ this.output);
            this.lfsr = shift(this.lfsr, getOutputLFSR() ^ this.output);
        }
        this.initialised = true;
    }

    private void oneRound() {
        this.output = getOutput();
        byte[] bArr = this.out;
        int i = this.output;
        bArr[0] = (byte) i;
        bArr[1] = (byte) (i >> 8);
        bArr[2] = (byte) (i >> 16);
        bArr[3] = (byte) (i >> 24);
        this.nfsr = shift(this.nfsr, getOutputNFSR() ^ this.lfsr[0]);
        this.lfsr = shift(this.lfsr, getOutputLFSR());
    }

    private void setKey(byte[] bArr, byte[] bArr2) {
        bArr2[12] = -1;
        bArr2[13] = -1;
        bArr2[14] = -1;
        bArr2[15] = -1;
        this.workingKey = bArr;
        this.workingIV = bArr2;
        int i = 0;
        int i2 = 0;
        while (true) {
            int[] iArr = this.nfsr;
            if (i < iArr.length) {
                byte[] bArr3 = this.workingKey;
                int i3 = i2 + 3;
                int i4 = i2 + 2;
                int i5 = i2 + 1;
                iArr[i] = (bArr3[i2] & 255) | (bArr3[i3] << 24) | ((bArr3[i4] << Tnaf.POW_2_WIDTH) & 16711680) | ((bArr3[i5] << 8) & 65280);
                int[] iArr2 = this.lfsr;
                byte[] bArr4 = this.workingIV;
                iArr2[i] = (bArr4[i2] & 255) | (bArr4[i3] << 24) | ((bArr4[i4] << Tnaf.POW_2_WIDTH) & 16711680) | ((bArr4[i5] << 8) & 65280);
                i2 += 4;
                i++;
            } else {
                return;
            }
        }
    }

    private int[] shift(int[] iArr, int i) {
        iArr[0] = iArr[1];
        iArr[1] = iArr[2];
        iArr[2] = iArr[3];
        iArr[3] = i;
        return iArr;
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public String getAlgorithmName() {
        return "Grain-128";
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv == null || iv.length != 12) {
                throw new IllegalArgumentException("Grain-128  requires exactly 12 bytes of IV");
            } else if (parametersWithIV.getParameters() instanceof KeyParameter) {
                KeyParameter keyParameter = (KeyParameter) parametersWithIV.getParameters();
                this.workingIV = new byte[keyParameter.getKey().length];
                this.workingKey = new byte[keyParameter.getKey().length];
                this.lfsr = new int[4];
                this.nfsr = new int[4];
                this.out = new byte[4];
                System.arraycopy(iv, 0, this.workingIV, 0, iv.length);
                System.arraycopy(keyParameter.getKey(), 0, this.workingKey, 0, keyParameter.getKey().length);
                reset();
            } else {
                throw new IllegalArgumentException("Grain-128 Init parameters must include a key");
            }
        } else {
            throw new IllegalArgumentException("Grain-128 Init parameters must include an IV");
        }
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
        if (!this.initialised) {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        } else if (i + i2 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i3 + i2 <= bArr2.length) {
            for (int i4 = 0; i4 < i2; i4++) {
                bArr2[i3 + i4] = (byte) (bArr[i + i4] ^ getKeyStream());
            }
            return i2;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public void reset() {
        this.index = 4;
        setKey(this.workingKey, this.workingIV);
        initGrain();
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public byte returnByte(byte b) {
        if (this.initialised) {
            return (byte) (b ^ getKeyStream());
        }
        throw new IllegalStateException(getAlgorithmName() + " not initialised");
    }
}
