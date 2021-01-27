package org.bouncycastle.crypto.engines;

import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class VMPCKSA3Engine extends VMPCEngine {
    @Override // org.bouncycastle.crypto.engines.VMPCEngine, org.bouncycastle.crypto.StreamCipher
    public String getAlgorithmName() {
        return "VMPC-KSA3";
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.engines.VMPCEngine
    public void initKey(byte[] bArr, byte[] bArr2) {
        this.s = 0;
        this.P = new byte[256];
        for (int i = 0; i < 256; i++) {
            this.P[i] = (byte) i;
        }
        for (int i2 = 0; i2 < 768; i2++) {
            byte[] bArr3 = this.P;
            byte b = this.s;
            byte[] bArr4 = this.P;
            int i3 = i2 & GF2Field.MASK;
            this.s = bArr3[(b + bArr4[i3] + bArr[i2 % bArr.length]) & GF2Field.MASK];
            byte b2 = this.P[i3];
            this.P[i3] = this.P[this.s & 255];
            this.P[this.s & 255] = b2;
        }
        for (int i4 = 0; i4 < 768; i4++) {
            byte[] bArr5 = this.P;
            byte b3 = this.s;
            byte[] bArr6 = this.P;
            int i5 = i4 & GF2Field.MASK;
            this.s = bArr5[(b3 + bArr6[i5] + bArr2[i4 % bArr2.length]) & GF2Field.MASK];
            byte b4 = this.P[i5];
            this.P[i5] = this.P[this.s & 255];
            this.P[this.s & 255] = b4;
        }
        for (int i6 = 0; i6 < 768; i6++) {
            byte[] bArr7 = this.P;
            byte b5 = this.s;
            byte[] bArr8 = this.P;
            int i7 = i6 & GF2Field.MASK;
            this.s = bArr7[(b5 + bArr8[i7] + bArr[i6 % bArr.length]) & GF2Field.MASK];
            byte b6 = this.P[i7];
            this.P[i7] = this.P[this.s & 255];
            this.P[this.s & 255] = b6;
        }
        this.n = 0;
    }
}
