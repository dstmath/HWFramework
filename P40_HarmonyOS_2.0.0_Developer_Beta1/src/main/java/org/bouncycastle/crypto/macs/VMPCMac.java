package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class VMPCMac implements Mac {
    private byte[] P = null;
    private byte[] T;
    private byte g;
    private byte n = 0;
    private byte s = 0;
    private byte[] workingIV;
    private byte[] workingKey;
    private byte x1;
    private byte x2;
    private byte x3;
    private byte x4;

    private void initKey(byte[] bArr, byte[] bArr2) {
        this.s = 0;
        this.P = new byte[256];
        for (int i = 0; i < 256; i++) {
            this.P[i] = (byte) i;
        }
        for (int i2 = 0; i2 < 768; i2++) {
            byte[] bArr3 = this.P;
            byte b = this.s;
            int i3 = i2 & GF2Field.MASK;
            this.s = bArr3[(b + bArr3[i3] + bArr[i2 % bArr.length]) & GF2Field.MASK];
            byte b2 = bArr3[i3];
            byte b3 = this.s;
            bArr3[i3] = bArr3[b3 & 255];
            bArr3[b3 & 255] = b2;
        }
        for (int i4 = 0; i4 < 768; i4++) {
            byte[] bArr4 = this.P;
            byte b4 = this.s;
            int i5 = i4 & GF2Field.MASK;
            this.s = bArr4[(b4 + bArr4[i5] + bArr2[i4 % bArr2.length]) & GF2Field.MASK];
            byte b5 = bArr4[i5];
            byte b6 = this.s;
            bArr4[i5] = bArr4[b6 & 255];
            bArr4[b6 & 255] = b5;
        }
        this.n = 0;
    }

    @Override // org.bouncycastle.crypto.Mac
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        for (int i2 = 1; i2 < 25; i2++) {
            byte[] bArr2 = this.P;
            byte b = this.s;
            byte b2 = this.n;
            this.s = bArr2[(b + bArr2[b2 & 255]) & GF2Field.MASK];
            byte b3 = this.x4;
            byte b4 = this.x3;
            this.x4 = bArr2[(b3 + b4 + i2) & GF2Field.MASK];
            byte b5 = this.x2;
            this.x3 = bArr2[(b4 + b5 + i2) & GF2Field.MASK];
            byte b6 = this.x1;
            this.x2 = bArr2[(b5 + b6 + i2) & GF2Field.MASK];
            byte b7 = this.s;
            this.x1 = bArr2[(b6 + b7 + i2) & GF2Field.MASK];
            byte[] bArr3 = this.T;
            byte b8 = this.g;
            bArr3[b8 & 31] = (byte) (bArr3[b8 & 31] ^ this.x1);
            bArr3[(b8 + 1) & 31] = (byte) (bArr3[(b8 + 1) & 31] ^ this.x2);
            bArr3[(b8 + 2) & 31] = (byte) (bArr3[(b8 + 2) & 31] ^ this.x3);
            bArr3[(b8 + 3) & 31] = (byte) (bArr3[(b8 + 3) & 31] ^ this.x4);
            this.g = (byte) ((b8 + 4) & 31);
            byte b9 = bArr2[b2 & 255];
            bArr2[b2 & 255] = bArr2[b7 & 255];
            bArr2[b7 & 255] = b9;
            this.n = (byte) ((b2 + 1) & GF2Field.MASK);
        }
        for (int i3 = 0; i3 < 768; i3++) {
            byte[] bArr4 = this.P;
            byte b10 = this.s;
            int i4 = i3 & GF2Field.MASK;
            this.s = bArr4[(b10 + bArr4[i4] + this.T[i3 & 31]) & GF2Field.MASK];
            byte b11 = bArr4[i4];
            byte b12 = this.s;
            bArr4[i4] = bArr4[b12 & 255];
            bArr4[b12 & 255] = b11;
        }
        byte[] bArr5 = new byte[20];
        for (int i5 = 0; i5 < 20; i5++) {
            byte[] bArr6 = this.P;
            byte b13 = this.s;
            int i6 = i5 & GF2Field.MASK;
            this.s = bArr6[(b13 + bArr6[i6]) & GF2Field.MASK];
            byte b14 = this.s;
            bArr5[i5] = bArr6[(bArr6[bArr6[b14 & 255] & 255] + 1) & GF2Field.MASK];
            byte b15 = bArr6[i6];
            bArr6[i6] = bArr6[b14 & 255];
            bArr6[b14 & 255] = b15;
        }
        System.arraycopy(bArr5, 0, bArr, i, bArr5.length);
        reset();
        return bArr5.length;
    }

    @Override // org.bouncycastle.crypto.Mac
    public String getAlgorithmName() {
        return "VMPC-MAC";
    }

    @Override // org.bouncycastle.crypto.Mac
    public int getMacSize() {
        return 20;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            KeyParameter keyParameter = (KeyParameter) parametersWithIV.getParameters();
            if (parametersWithIV.getParameters() instanceof KeyParameter) {
                this.workingIV = parametersWithIV.getIV();
                byte[] bArr = this.workingIV;
                if (bArr == null || bArr.length < 1 || bArr.length > 768) {
                    throw new IllegalArgumentException("VMPC-MAC requires 1 to 768 bytes of IV");
                }
                this.workingKey = keyParameter.getKey();
                reset();
                return;
            }
            throw new IllegalArgumentException("VMPC-MAC Init parameters must include a key");
        }
        throw new IllegalArgumentException("VMPC-MAC Init parameters must include an IV");
    }

    @Override // org.bouncycastle.crypto.Mac
    public void reset() {
        initKey(this.workingKey, this.workingIV);
        this.n = 0;
        this.x4 = 0;
        this.x3 = 0;
        this.x2 = 0;
        this.x1 = 0;
        this.g = 0;
        this.T = new byte[32];
        for (int i = 0; i < 32; i++) {
            this.T[i] = 0;
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte b) throws IllegalStateException {
        byte[] bArr = this.P;
        byte b2 = this.s;
        byte b3 = this.n;
        this.s = bArr[(b2 + bArr[b3 & 255]) & GF2Field.MASK];
        byte b4 = this.s;
        byte b5 = this.x4;
        byte b6 = this.x3;
        this.x4 = bArr[(b5 + b6) & GF2Field.MASK];
        byte b7 = this.x2;
        this.x3 = bArr[(b6 + b7) & GF2Field.MASK];
        byte b8 = this.x1;
        this.x2 = bArr[(b7 + b8) & GF2Field.MASK];
        this.x1 = bArr[(b8 + b4 + ((byte) (b ^ bArr[(bArr[bArr[b4 & 255] & 255] + 1) & GF2Field.MASK]))) & GF2Field.MASK];
        byte[] bArr2 = this.T;
        byte b9 = this.g;
        bArr2[b9 & 31] = (byte) (bArr2[b9 & 31] ^ this.x1);
        bArr2[(b9 + 1) & 31] = (byte) (bArr2[(b9 + 1) & 31] ^ this.x2);
        bArr2[(b9 + 2) & 31] = (byte) (bArr2[(b9 + 2) & 31] ^ this.x3);
        bArr2[(b9 + 3) & 31] = (byte) (bArr2[(b9 + 3) & 31] ^ this.x4);
        this.g = (byte) ((b9 + 4) & 31);
        byte b10 = bArr[b3 & 255];
        bArr[b3 & 255] = bArr[b4 & 255];
        bArr[b4 & 255] = b10;
        this.n = (byte) ((b3 + 1) & GF2Field.MASK);
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte[] bArr, int i, int i2) throws DataLengthException, IllegalStateException {
        if (i + i2 <= bArr.length) {
            for (int i3 = 0; i3 < i2; i3++) {
                update(bArr[i + i3]);
            }
            return;
        }
        throw new DataLengthException("input buffer too short");
    }
}
