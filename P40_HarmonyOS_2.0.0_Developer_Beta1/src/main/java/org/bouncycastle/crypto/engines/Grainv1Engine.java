package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.digests.Blake2xsDigest;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class Grainv1Engine implements StreamCipher {
    private static final int STATE_SIZE = 5;
    private int index = 2;
    private boolean initialised = false;
    private int[] lfsr;
    private int[] nfsr;
    private byte[] out;
    private int output;
    private byte[] workingIV;
    private byte[] workingKey;

    private byte getKeyStream() {
        if (this.index > 1) {
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
        int i = (iArr[0] >>> 1) | (iArr[1] << 15);
        int i2 = (iArr[0] >>> 2) | (iArr[1] << 14);
        int i3 = (iArr[0] >>> 4) | (iArr[1] << 12);
        int i4 = (iArr[0] >>> 10) | (iArr[1] << 6);
        int i5 = (iArr[1] >>> 15) | (iArr[2] << 1);
        int i6 = (iArr[2] >>> 11) | (iArr[3] << 5);
        int i7 = (iArr[3] >>> 8) | (iArr[4] << 8);
        int i8 = (iArr[4] << 1) | (iArr[3] >>> 15);
        int[] iArr2 = this.lfsr;
        int i9 = (iArr2[0] >>> 3) | (iArr2[1] << 13);
        int i10 = (iArr2[1] >>> 9) | (iArr2[2] << 7);
        int i11 = (iArr2[3] << 2) | (iArr2[2] >>> 14);
        int i12 = iArr2[4];
        int i13 = (i10 ^ i8) ^ (i9 & i12);
        int i14 = i11 & i12;
        int i15 = i9 & i11;
        int i16 = i12 & i15;
        int i17 = i15 & i8;
        return (((((((((i8 & i14) ^ ((i17 ^ (i16 ^ (((i13 ^ i14) ^ (i12 & i8)) ^ ((i9 & i10) & i11)))) ^ ((i10 & i11) & i8))) ^ i) ^ i2) ^ i3) ^ i4) ^ i5) ^ i6) ^ i7) & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
    }

    private int getOutputLFSR() {
        int[] iArr = this.lfsr;
        int i = iArr[0];
        int i2 = (iArr[1] >>> 7) | (iArr[2] << 9);
        int i3 = (iArr[2] >>> 6) | (iArr[3] << 10);
        return (((iArr[4] << 2) | (iArr[3] >>> 14)) ^ ((((((iArr[0] >>> 13) | (iArr[1] << 3)) ^ i) ^ i2) ^ i3) ^ ((iArr[3] >>> 3) | (iArr[4] << 13)))) & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
    }

    private int getOutputNFSR() {
        int[] iArr = this.nfsr;
        int i = iArr[0];
        int i2 = (iArr[0] >>> 9) | (iArr[1] << 7);
        int i3 = (iArr[0] >>> 14) | (iArr[1] << 2);
        int i4 = (iArr[0] >>> 15) | (iArr[1] << 1);
        int i5 = (iArr[1] >>> 5) | (iArr[2] << 11);
        int i6 = (iArr[1] >>> 12) | (iArr[2] << 4);
        int i7 = (iArr[2] >>> 1) | (iArr[3] << 15);
        int i8 = (iArr[2] >>> 5) | (iArr[3] << 11);
        int i9 = (iArr[2] >>> 13) | (iArr[3] << 3);
        int i10 = (iArr[3] >>> 4) | (iArr[4] << 12);
        int i11 = (iArr[3] >>> 12) | (iArr[4] << 4);
        int i12 = (iArr[4] << 1) | (iArr[3] >>> 15);
        int i13 = i12 & i11;
        int i14 = i11 & i10;
        int i15 = ((((i ^ (((((((((((iArr[3] >>> 14) | (iArr[4] << 2)) ^ i11) ^ i10) ^ i9) ^ i8) ^ i7) ^ i6) ^ i5) ^ i3) ^ i2)) ^ i13) ^ (i8 & i7)) ^ (i4 & i2)) ^ (i14 & i9);
        int i16 = i7 & i6 & i5;
        return (((((((((i12 & i9) & i6) & i2) ^ (i15 ^ i16)) ^ ((i14 & i8) & i7)) ^ ((i13 & i5) & i4)) ^ (((i13 & i10) & i9) & i8)) ^ ((i4 & i16) & i2)) ^ (((((i10 & i9) & i8) & i7) & i6) & i5)) & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
    }

    private void initGrain() {
        for (int i = 0; i < 10; i++) {
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
        this.nfsr = shift(this.nfsr, getOutputNFSR() ^ this.lfsr[0]);
        this.lfsr = shift(this.lfsr, getOutputLFSR());
    }

    private void setKey(byte[] bArr, byte[] bArr2) {
        bArr2[8] = -1;
        bArr2[9] = -1;
        this.workingKey = bArr;
        this.workingIV = bArr2;
        int i = 0;
        int i2 = 0;
        while (true) {
            int[] iArr = this.nfsr;
            if (i < iArr.length) {
                byte[] bArr3 = this.workingKey;
                int i3 = i2 + 1;
                iArr[i] = ((bArr3[i2] & 255) | (bArr3[i3] << 8)) & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
                int[] iArr2 = this.lfsr;
                byte[] bArr4 = this.workingIV;
                iArr2[i] = ((bArr4[i2] & 255) | (bArr4[i3] << 8)) & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
                i2 += 2;
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
        iArr[3] = iArr[4];
        iArr[4] = i;
        return iArr;
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public String getAlgorithmName() {
        return "Grain v1";
    }

    @Override // org.bouncycastle.crypto.StreamCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv == null || iv.length != 8) {
                throw new IllegalArgumentException("Grain v1 requires exactly 8 bytes of IV");
            } else if (parametersWithIV.getParameters() instanceof KeyParameter) {
                KeyParameter keyParameter = (KeyParameter) parametersWithIV.getParameters();
                this.workingIV = new byte[keyParameter.getKey().length];
                this.workingKey = new byte[keyParameter.getKey().length];
                this.lfsr = new int[5];
                this.nfsr = new int[5];
                this.out = new byte[2];
                System.arraycopy(iv, 0, this.workingIV, 0, iv.length);
                System.arraycopy(keyParameter.getKey(), 0, this.workingKey, 0, keyParameter.getKey().length);
                reset();
            } else {
                throw new IllegalArgumentException("Grain v1 Init parameters must include a key");
            }
        } else {
            throw new IllegalArgumentException("Grain v1 Init parameters must include an IV");
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
        this.index = 2;
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
