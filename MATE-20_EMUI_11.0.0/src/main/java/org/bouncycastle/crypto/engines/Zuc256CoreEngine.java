package org.bouncycastle.crypto.engines;

import org.bouncycastle.math.ec.Tnaf;
import org.bouncycastle.util.Memoable;

public class Zuc256CoreEngine extends Zuc128CoreEngine {
    private static final byte[] EK_d = {34, 47, 36, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, Tnaf.POW_2_WIDTH, 48};
    private static final byte[] EK_d128 = {35, 47, 37, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, Tnaf.POW_2_WIDTH, 48};
    private static final byte[] EK_d32 = {34, 47, 37, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, Tnaf.POW_2_WIDTH, 48};
    private static final byte[] EK_d64 = {35, 47, 36, 42, 109, 64, 64, 64, 64, 64, 64, 64, 64, 82, Tnaf.POW_2_WIDTH, 48};
    private byte[] theD;

    protected Zuc256CoreEngine() {
        this.theD = EK_d;
    }

    protected Zuc256CoreEngine(int i) {
        byte[] bArr;
        if (i == 32) {
            bArr = EK_d32;
        } else if (i == 64) {
            bArr = EK_d64;
        } else if (i == 128) {
            bArr = EK_d128;
        } else {
            throw new IllegalArgumentException("Unsupported length: " + i);
        }
        this.theD = bArr;
    }

    protected Zuc256CoreEngine(Zuc256CoreEngine zuc256CoreEngine) {
        super(zuc256CoreEngine);
    }

    private static int MAKEU31(byte b, byte b2, byte b3, byte b4) {
        return ((b & 255) << 23) | ((b2 & 255) << 16) | ((b3 & 255) << 8) | (b4 & 255);
    }

    @Override // org.bouncycastle.crypto.engines.Zuc128CoreEngine, org.bouncycastle.util.Memoable
    public Memoable copy() {
        return new Zuc256CoreEngine(this);
    }

    @Override // org.bouncycastle.crypto.engines.Zuc128CoreEngine, org.bouncycastle.crypto.StreamCipher
    public String getAlgorithmName() {
        return "Zuc-256";
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.engines.Zuc128CoreEngine
    public int getMaxIterations() {
        return 625;
    }

    @Override // org.bouncycastle.crypto.engines.Zuc128CoreEngine, org.bouncycastle.util.Memoable
    public void reset(Memoable memoable) {
        super.reset(memoable);
        this.theD = ((Zuc256CoreEngine) memoable).theD;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.engines.Zuc128CoreEngine
    public void setKeyAndIV(int[] iArr, byte[] bArr, byte[] bArr2) {
        if (bArr == null || bArr.length != 32) {
            throw new IllegalArgumentException("A key of 32 bytes is needed");
        } else if (bArr2 == null || bArr2.length != 25) {
            throw new IllegalArgumentException("An IV of 25 bytes is needed");
        } else {
            iArr[0] = MAKEU31(bArr[0], this.theD[0], bArr[21], bArr[16]);
            iArr[1] = MAKEU31(bArr[1], this.theD[1], bArr[22], bArr[17]);
            iArr[2] = MAKEU31(bArr[2], this.theD[2], bArr[23], bArr[18]);
            iArr[3] = MAKEU31(bArr[3], this.theD[3], bArr[24], bArr[19]);
            iArr[4] = MAKEU31(bArr[4], this.theD[4], bArr[25], bArr[20]);
            iArr[5] = MAKEU31(bArr2[0], (byte) (this.theD[5] | (bArr2[17] & 63)), bArr[5], bArr[26]);
            iArr[6] = MAKEU31(bArr2[1], (byte) (this.theD[6] | (bArr2[18] & 63)), bArr[6], bArr[27]);
            iArr[7] = MAKEU31(bArr2[10], (byte) (this.theD[7] | (bArr2[19] & 63)), bArr[7], bArr2[2]);
            iArr[8] = MAKEU31(bArr[8], (byte) (this.theD[8] | (bArr2[20] & 63)), bArr2[3], bArr2[11]);
            iArr[9] = MAKEU31(bArr[9], (byte) (this.theD[9] | (bArr2[21] & 63)), bArr2[12], bArr2[4]);
            iArr[10] = MAKEU31(bArr2[5], (byte) (this.theD[10] | (bArr2[22] & 63)), bArr[10], bArr[28]);
            iArr[11] = MAKEU31(bArr[11], (byte) (this.theD[11] | (bArr2[23] & 63)), bArr2[6], bArr2[13]);
            iArr[12] = MAKEU31(bArr[12], (byte) (this.theD[12] | (bArr2[24] & 63)), bArr2[7], bArr2[14]);
            iArr[13] = MAKEU31(bArr[13], this.theD[13], bArr2[15], bArr2[8]);
            iArr[14] = MAKEU31(bArr[14], (byte) (this.theD[14] | ((bArr[31] >>> 4) & 15)), bArr2[16], bArr2[9]);
            iArr[15] = MAKEU31(bArr[15], (byte) (this.theD[15] | (bArr[31] & 15)), bArr[30], bArr[29]);
        }
    }
}
